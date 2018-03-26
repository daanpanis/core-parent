package com.daanpanis.core.command;

import com.daanpanis.core.api.command.CommandArgument;

import java.util.Collection;
import java.util.stream.Collectors;

public class StaticCommandArgument implements CommandArgument {

    private final String name;
    private final Collection<String> values;

    public StaticCommandArgument(String name, Collection<String> values) {
        this.name = name;
        this.values = values.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }

    public Collection<String> getValues() {
        return values;
    }

    public boolean matches(String value) {
        return values.contains(value.toLowerCase());
    }

    @Override
    public String getName() {
        return name;
    }
}
