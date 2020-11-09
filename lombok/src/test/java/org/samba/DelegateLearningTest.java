package org.samba;

import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class DelegateLearningTest {

    @Test
    public void composeClassOutOfTwoOtherClasses() {
        var greeter = new Greeter();
        greeter.sayHi();
        greeter.sayYes();
    }

    class Greeter {
        @Delegate HiSayer hiSayer;
        @Delegate YesSayer yesSayer;
    }

    class HiSayer {
        public void sayHi() {
            System.out.println("Hi");
        }
    }

    class YesSayer {
        public void sayYes() {
            System.out.println("Yes");
        }
    }
}
