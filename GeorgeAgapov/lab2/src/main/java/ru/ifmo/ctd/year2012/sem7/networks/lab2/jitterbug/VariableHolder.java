package ru.ifmo.ctd.year2012.sem7.networks.lab2.jitterbug;

interface VariableHolder<D extends Data<D>> {
    D getData();
    int getTokenId();
}
