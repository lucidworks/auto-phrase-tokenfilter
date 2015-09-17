package com.lucidworks.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.Version;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class AutoPhrasingQParserPlugin extends QParserPlugin implements ResourceLoaderAware {

    private static final Logger Log = LoggerFactory.getLogger(AutoPhrasingQParserPlugin.class);
    private CharArraySet phraseSets;

    private AutoPhrasingParameters autoPhrasingParameters;

    @Override
    public void init(NamedList initArgs) {
        Log.debug("init AutoPhrasingQParserPlugin...");
        SolrParams solrParams = SolrParams.toSolrParams(initArgs);
        autoPhrasingParameters = new AutoPhrasingParameters(solrParams);
    }

    @Override
    public QParser createParser(String qStr, SolrParams localParams, SolrParams params,
                                SolrQueryRequest req) {
        Log.debug("createParser AutoPhrasingQParserPlugin...");
        ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams(params);

        String modQ = qStr;
        if (qStr != null) {
            modQ = filter(qStr);
            modifiableSolrParams.set("q", modQ);
        }
        return req.getCore().getQueryPlugin(autoPhrasingParameters.getDownstreamParser())
                .createParser(modQ, localParams, modifiableSolrParams, req);
    }

    private String filter(String qStr) {

        String query = qStr;

        // filter : for field names
        while (query.contains(" :"))
            query = query.replaceAll("\\s:", ": ");

        // mandatory and optional clauses
        query = query.replaceAll("\\+", "+ ");
        query = query.replaceAll("\\-", "- ");

        // logical operators
        if (autoPhrasingParameters.getIgnoreCase()) {
            query = query.replaceAll("AND", "&&");
            query = query.replaceAll("OR", "||");
            query = query.replaceAll("TO", "`to`");
            query = query.replaceAll("NOW", "`now`");
        }

        // grouping with parenthesis
        query = query.replaceAll("\\(", "( ");
        query = query.replaceAll("\\)", " )");

        // phrases with quotes
        query = String.format(" %s ", query);
        query = query.replaceAll("(^|\\s)\"", " open_quote` ");
        query = query.replaceAll("\"(\\s|$)", " close_quote` ");

        // regular expression queries
        query = query.replaceAll("(?i)(\\d+)\\s?(pound[s]?|lb[s]?)", "$1lb");
        query = query.replaceAll("(?i)(\\d+)\\s?(inch(es)?|in?)", "$1in");
        query = query.replaceAll("(?i)(\\d+)\\s?(ounce[s]?|oz)", "$1oz");
        query = query.replaceAll("(?i)(\\d+)\\s?(quart[s]?|qt[s]?)", "$1qt");
        query = query.replaceAll("(?i)(\\d+)\\s?(gallon[s]?|gal?)", "$1gal");
        query = query.replaceAll("(?i)(\\d+)\\s?(yd[s]?|yard[s]?)", "$1yd");
        query = query.replaceAll("(?i)(\\d+)\\s?(mm|cc|ml)", "$1$2");

        // autophrase the query
        try {
            query = autophrase(query);
        } catch (IOException ioe) {
            Log.error(ioe.toString());
        }

        // restore mandatory and optional
        query = query.replaceAll("\\+ ", "+");
        query = query.replaceAll("\\- ", "-");

        // restore logical operators
        if (autoPhrasingParameters.getIgnoreCase()) {
            query = query.replaceAll("&&", "AND");
            query = query.replaceAll("\\|\\|", "OR");
            query = query.replaceAll("`to`", "TO");
            query = query.replaceAll("`now`", "NOW");
        }

        // restore grouping with parenthesis
        query = query.replaceAll( "\\( ", "(" );
        query = query.replaceAll( " \\)", ")" );

        // restore quotes
        query = query.replaceAll("open_quote`\\s", "\"");
        query = query.replaceAll("\\sclose_quote`", "\"");

        return query;
    }

    private String autophrase(String input) throws IOException {
        WhitespaceTokenizer wt = new WhitespaceTokenizer(Version.LUCENE_48, new StringReader(input));
        TokenStream ts = wt;
        if (autoPhrasingParameters.getIgnoreCase()) {
            ts = new LowerCaseFilter(Version.LUCENE_48, wt);
        }
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(Version.LUCENE_48, ts, phraseSets);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith(autoPhrasingParameters.getReplaceWhitespaceWith());
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        StringBuilder stringBuilder = new StringBuilder();
        while (autoPhrasingTokenFilter.incrementToken()) {
            stringBuilder.append(term.toString()).append(" ");
        }

        return stringBuilder.toString().trim();
    }

    @Override
    public void inform(ResourceLoader loader) throws IOException {
        List<String> phraseSetFiles = autoPhrasingParameters.getIndividualPhraseSetFiles();
        phraseSets = getWordSet(loader, phraseSetFiles, true);
    }

    private CharArraySet getWordSet(ResourceLoader loader,
                                    List<String> files, boolean ignoreCase)
            throws IOException {

        CharArraySet words = null;
        if (files.size() > 0) {
            // default stop words list has 35 or so words, but maybe don't make it that
            // big to start
            words = new CharArraySet(Version.LUCENE_48,
                    files.size() * 10, ignoreCase);
            for (String file : files) {
                List<String> stopWords = getLines(loader, file.trim());
                words.addAll(StopFilter.makeStopSet(Version.LUCENE_48, stopWords, ignoreCase));
            }
        }
        return words;
    }

    private List<String> getLines(ResourceLoader loader, String resource) throws IOException {
        return WordlistLoader.getLines(loader.openResource(resource), StandardCharsets.UTF_8);
    }

    /**
     * Returns the phrases that were loaded, intended for testing only
     * @return The phrase set
     */
    public CharArraySet getPhrases(){
        return phraseSets;
    }
}