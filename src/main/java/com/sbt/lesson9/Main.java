package com.sbt.lesson9;

import com.sbt.lesson9.cacheproxy.*;
import com.sbt.lesson9.services.*;

public class Main {
    public static void main(String[] args) {

        ICalculator proxyCalc = CacheProxy.cache(new CalculatorImpl(),
                "E:/TestProxy");

        System.out.println(proxyCalc.calc("first", 1, 1));
        System.out.println(proxyCalc.calc("second", 3, 4));
        System.out.println(proxyCalc.calc("tree", 5, 6));
        System.out.println(proxyCalc.calc("four", 7, 8));
        System.out.println(proxyCalc.calc("five", 9, 10));

    }
}
