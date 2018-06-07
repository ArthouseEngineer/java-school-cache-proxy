package com.sbt.lesson9.services;

public class CalculatorImpl implements ICalculator {
    @Override
    public int calc(String name, int arg1, int arg2) {
        return arg1 + arg2;
    }
}
