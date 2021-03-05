package org.samba;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;

public class MultimapLearningTest {

    @Test
    public void setMultimapRemovesDuplicateKeyValuePairs() {
        SetMultimap<String, String> multimap = Multimaps.synchronizedSetMultimap(Multimaps.newSetMultimap(new HashMap<>(), HashSet::new));
        multimap.put("Jonny", "Marr");
        multimap.put("Jonny", "Marr");
        multimap.put("Jonny", "Schmidt");
        multimap.put("Ian", "Curtis");

        assertThat(multimap.entries()).containsExactlyInAnyOrder(
                entry("Jonny", "Marr"),
                entry("Jonny", "Schmidt"),
                entry("Ian", "Curtis"));
    }

}
