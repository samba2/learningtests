package org.samba;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class TableLearningTest {

    //
    //  +-- row       +-- column            +--- value
    //  v             v                     v
    // Beer Type   | Name              | Price in Cent
    // Pilsner     | Stoertebecker     | 140
    // Pilsner     | Sterni            | 100
    // Pilsner     | Ur-Krostiz        | 120
    // Ale         | Stoertebecker     | 140
    // Ale         | The Funky Hopper  | 270

    @Test
    public void accessingTheSpaetiTable() {
        Table<String, String, Integer> spaeti = HashBasedTable.create();
        spaeti.put("Pilsner", "Stoertebecker", 140);
        spaeti.put("Pilsner", "Sterni", 100);
        spaeti.put("Pilsner", "Ur-Krostizer", 120);
        spaeti.put("Ale", "Stoertebecker", 140);
        spaeti.put("Ale", "The Funky Hopper", 270);

        assertThat(spaeti.size())
                .as("total entries")
                .isEqualTo(5);

        assertThat(spaeti.get("Pilsner", "Stoertebecker"))
                .as("Retrieving a single value")
                .isEqualTo(140);

        assertThat(spaeti.rowKeySet())
                .as("Number of different beer types")
                .hasSize(2)
                .containsExactly("Pilsner", "Ale");

        assertThat(spaeti.row("Pilsner"))
                .as("Number of Pilsner")
                .hasSize(3);

        assertThat(spaeti.row("Ale"))
                .as("Number of Ale")
                .hasSize(2);

        assertThat(spaeti.column("Stoertebecker"))
                .as("Different Stoertebecker beer types")
                .containsOnlyKeys("Pilsner", "Ale");
    }

}
