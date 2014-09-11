package com.lucidworks.analysis;

import org.apache.solr.common.params.SolrParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstracts the parameters (and the parsing of those parameters) used by the auto phrasing parser
 */
@SuppressWarnings("FieldCanBeLocal")
public class AutoPhrasingParameters {
    private final String DefaultDownstreamParser = "lucene";
    private final Character DefaultReplaceWhitespaceWith = null;
    private final boolean DefaultIgnoreCase = true;
    private final boolean DefaultEmitSingleTokens = false;

    public AutoPhrasingParameters(SolrParams solrParams) {
        if (solrParams == null) {
            downstreamParser = DefaultDownstreamParser;
            replaceWhitespaceWith = DefaultReplaceWhitespaceWith;
            ignoreCase = DefaultIgnoreCase;
            phraseSetFiles = null;
            emitSingleTokens = DefaultEmitSingleTokens;
        } else {
            setDownstreamParser(solrParams.get("defType", DefaultDownstreamParser));
            setReplaceWhitespaceWith(solrParams.get("replaceWhitespaceWith", null));
            setIgnoreCase(solrParams.getBool("ignoreCase", DefaultIgnoreCase));
            setPhraseSetFiles(solrParams.get("phrases"));
            setEmitSingleTokens(solrParams.getBool("includeTokens", DefaultEmitSingleTokens));
        }
    }

    /**
     * Getter for a comma separated string of file names containing auto phrases
     * @return a list of the parsed individual file names
     */
    public List<String> getIndividualPhraseSetFiles() {
        if (phraseSetFiles == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        for (String file : phraseSetFiles.split("(?<!\\\\),")) {
            result.add(file.replaceAll("\\\\(?=,)", ""));
        }

        return result;
    }

    public String getPhraseSetFiles() {
        return this.phraseSetFiles;
    }

    /**
     * Setter for a comma separated string of files containing autophrase entries
     * @param phraseSetFiles The comma separated string of files containing autophrase entries
     */
    public void setPhraseSetFiles(String phraseSetFiles) {
        this.phraseSetFiles = phraseSetFiles;
    }

    public Character getReplaceWhitespaceWith() {
        return replaceWhitespaceWith;
    }

    public void setReplaceWhitespaceWith(String replaceWhitespaceWith) {
        if (replaceWhitespaceWith != null && replaceWhitespaceWith.length() > 0)
            this.replaceWhitespaceWith = replaceWhitespaceWith.charAt(0);
        else
            this.replaceWhitespaceWith = DefaultReplaceWhitespaceWith;
    }

    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public String getDownstreamParser() {
        return downstreamParser;
    }

    public void setDownstreamParser(String downstreamParser) {
        this.downstreamParser = downstreamParser;
    }

    public boolean getEmitSingleTokens() {
        return emitSingleTokens;
    }

    public void setEmitSingleTokens(boolean emitSingleTokens) {
        this.emitSingleTokens = emitSingleTokens;
    }

    private String downstreamParser;
    private Character replaceWhitespaceWith;
    private boolean ignoreCase;
    private String phraseSetFiles;
    private boolean emitSingleTokens;
}
