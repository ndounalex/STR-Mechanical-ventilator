package com.udes.model.astd.tojson;

import java.util.List;

public class ToJson {
    private String nodeIndex;
    private List<String> subNodeIndex;

    public String getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(String nodeIdx) {
        this.nodeIndex = nodeIdx;
    }

    public List<String> getSubNodeIndex() {
        return subNodeIndex;
    }

    public void setSubNodeIndex(List<String> nodeIdx) {
        this.subNodeIndex = nodeIdx;
    }
}
