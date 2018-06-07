package com.sbt.lesson9.services;

import com.sbt.lesson9.cacheproxy.*;

public interface ICalculator {
    @Cache(cacheType = CacheType.FILE, fileNamePrefix = "cahce_calc",zip = true,identityBy = {0,2})
    int calc(String name, int arg1, int arg2);
}
