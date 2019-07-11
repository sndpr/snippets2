package org.psc.fieldfilter;

public interface JsonFilter<T, U> {

    void setFilterValue(U filterValue);

    T filter(T instance);
}
