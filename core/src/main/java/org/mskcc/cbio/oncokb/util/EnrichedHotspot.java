package org.mskcc.cbio.oncokb.util;

import org.genome_nexus.client.*;

import static org.mskcc.cbio.oncokb.util.HotspotUtils.extractProteinPos;

public class EnrichedHotspot extends Hotspot {
    Integer start;
    Integer end;

    public EnrichedHotspot(Hotspot hotspot) {
        this.setHugoSymbol(hotspot.getHugoSymbol());
        this.setType(hotspot.getType());
        this.setResidue(hotspot.getResidue());
        this.setTranscriptId(hotspot.getTranscriptId());
        this.setInframeCount(hotspot.getInframeCount());
        this.setTumorCount(hotspot.getTumorCount());
        this.setTruncatingCount(hotspot.getTruncatingCount());
        this.setSpliceCount(hotspot.getSpliceCount());

        // Protein location
        IntegerRange integerRange = extractProteinPos(this.getResidue());
        this.setStart(integerRange.getStart());
        this.setEnd(integerRange.getEnd());
    }


    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }
}
