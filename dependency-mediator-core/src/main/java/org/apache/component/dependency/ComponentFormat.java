package org.apache.component.dependency;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:fengjia10@gmail.com">Von Gosling</a>
 */
public enum ComponentFormat {
    CLASS("class"),
    JAR("jar"),
    WAR("war"),
    EAR("ear"),
    SAR("sar"),
    ZIP("zip"),
    GZIP("gzip");

    private String value;

    ComponentFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    private static Map<String, ComponentFormat> stringToEnum = new HashMap<String, ComponentFormat>();

    static {
        for (ComponentFormat fileFormat : values()) {
            stringToEnum.put(fileFormat.getValue(), fileFormat);
        }
    }

    public static ComponentFormat fromString(String value) {
        return stringToEnum.get(value.toLowerCase());
    }
}
