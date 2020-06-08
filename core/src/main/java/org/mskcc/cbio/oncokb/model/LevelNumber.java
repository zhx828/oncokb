package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import java.util.Set;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class LevelNumber {

    private Set<Gene> genes = null;
    private LevelOfEvidence level = null;
    private LevelNumberType type;

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("genes")
    public Set<Gene> getGenes() {
        return genes;
    }

    public void setGenes(Set<Gene> genes) {
        this.genes = genes;
    }


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("level")
    public LevelOfEvidence getLevel() {
        return level;
    }

    public void setLevel(LevelOfEvidence level) {
        this.level = level;
    }

    public LevelNumberType getType() {
        return type;
    }

    public void setType(LevelNumberType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LevelNumber)) return false;
        LevelNumber that = (LevelNumber) o;
        return Objects.equals(getGenes(), that.getGenes()) &&
            getLevel() == that.getLevel() &&
            getType() == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGenes(), getLevel(), getType());
    }

    @Override
    public String toString() {
        return "LevelNumber{" +
            "genes=" + genes +
            ", level=" + level +
            ", type=" + type +
            '}';
    }
}
