package com.udes.parser;

import com.udes.model.il.terms.Bool;

public interface Parser<T> {
   public T parse(Bool b);
}