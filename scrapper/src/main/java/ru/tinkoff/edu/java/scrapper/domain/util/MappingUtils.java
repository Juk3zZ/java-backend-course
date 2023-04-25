package ru.tinkoff.edu.java.scrapper.domain.util;

import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.scrapper.domain.model.TableLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MappingUtils {

    public Map<TableLink, List<Long>> createMapFromEntries(List<Map.Entry<TableLink, Long>> entries) {
        var result = new HashMap<TableLink, List<Long>>();
        for (var entry : entries) {
            var list = result.get(entry.getKey());
            if (list == null) {
                var ids = new ArrayList<Long>();
                ids.add(entry.getValue());
                result.put(entry.getKey(), ids);
            } else {
                list.add(entry.getValue());
            }
        }
        return result;
    }
}
