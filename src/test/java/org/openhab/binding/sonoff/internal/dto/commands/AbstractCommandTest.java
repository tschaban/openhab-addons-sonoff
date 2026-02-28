/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.sonoff.internal.dto.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AbstractCommand}.
 *
 * Tests cover:
 * - Abstract class contract with concrete implementations
 * - Generic type safety and return values
 * - getCommand() method behavior in subclasses
 * - Inheritance hierarchy validation
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class AbstractCommandTest {

    /**
     * Concrete test implementation of AbstractCommand for String type
     */
    private static class StringCommand extends AbstractCommand<StringCommand> {
        private String value = "test";

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public StringCommand getCommand() {
            return this;
        }
    }

    /**
     * Concrete test implementation of AbstractCommand for Integer type
     */
    private static class IntegerCommand extends AbstractCommand<IntegerCommand> {
        private Integer value = 42;

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        @Override
        public IntegerCommand getCommand() {
            return this;
        }
    }

    /**
     * Test implementation with nested data structure
     */
    private static class ComplexCommand extends AbstractCommand<ComplexCommand> {
        private String name;
        private Integer count;
        private Boolean enabled;

        public ComplexCommand(String name, Integer count, Boolean enabled) {
            this.name = name;
            this.count = count;
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public Integer getCount() {
            return count;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        @Override
        public ComplexCommand getCommand() {
            return this;
        }
    }

    @Test
    @DisplayName("Should verify StringCommand extends AbstractCommand")
    void testStringCommandInheritance() {
        StringCommand cmd = new StringCommand();

        // Verify inheritance
        assertTrue(cmd instanceof AbstractCommand, "StringCommand should extend AbstractCommand");

        // Verify getCommand returns correct instance
        AbstractCommand<StringCommand> abstractCmd = cmd;
        StringCommand result = abstractCmd.getCommand();

        assertNotNull(result, "getCommand() should not return null");
        assertSame(cmd, result, "getCommand() should return the same instance");
    }

    @Test
    @DisplayName("Should verify IntegerCommand extends AbstractCommand")
    void testIntegerCommandInheritance() {
        IntegerCommand cmd = new IntegerCommand();

        // Verify inheritance
        assertTrue(cmd instanceof AbstractCommand, "IntegerCommand should extend AbstractCommand");

        // Verify getCommand returns correct instance
        AbstractCommand<IntegerCommand> abstractCmd = cmd;
        IntegerCommand result = abstractCmd.getCommand();

        assertNotNull(result, "getCommand() should not return null");
        assertSame(cmd, result, "getCommand() should return the same instance");
    }

    @Test
    @DisplayName("Should verify ComplexCommand extends AbstractCommand")
    void testComplexCommandInheritance() {
        ComplexCommand cmd = new ComplexCommand("TestCommand", 10, true);

        // Verify inheritance
        assertTrue(cmd instanceof AbstractCommand, "ComplexCommand should extend AbstractCommand");

        // Verify getCommand returns correct instance
        AbstractCommand<ComplexCommand> abstractCmd = cmd;
        ComplexCommand result = abstractCmd.getCommand();

        assertNotNull(result, "getCommand() should not return null");
        assertSame(cmd, result, "getCommand() should return the same instance");
    }

    @Test
    @DisplayName("Should return self-reference from getCommand")
    void testGetCommandReturnsSelf() {
        StringCommand cmd = new StringCommand();
        StringCommand result = cmd.getCommand();

        assertSame(cmd, result, "getCommand() should return self-reference");
        assertEquals("test", result.getValue(), "Returned instance should maintain state");
    }

    @Test
    @DisplayName("Should maintain state through getCommand call")
    void testStatePersistenceThroughGetCommand() {
        StringCommand cmd = new StringCommand();
        cmd.setValue("modified");

        StringCommand result = cmd.getCommand();

        assertEquals("modified", result.getValue(), "State should persist through getCommand()");
        assertSame(cmd, result, "Should be same instance");
    }

    @Test
    @DisplayName("Should support method chaining pattern")
    void testMethodChainingPattern() {
        IntegerCommand cmd = new IntegerCommand();
        cmd.setValue(100);

        // Simulate method chaining using getCommand()
        IntegerCommand result = cmd.getCommand();
        result.setValue(200);

        assertEquals(200, cmd.getValue(), "Changes through returned command should affect original");
        assertSame(cmd, result, "Should be same instance for chaining");
    }

    @Test
    @DisplayName("Should handle multiple getCommand calls")
    void testMultipleGetCommandCalls() {
        StringCommand cmd = new StringCommand();

        StringCommand first = cmd.getCommand();
        StringCommand second = cmd.getCommand();
        StringCommand third = first.getCommand();

        // All calls should return the same instance
        assertSame(cmd, first, "First call should return same instance");
        assertSame(cmd, second, "Second call should return same instance");
        assertSame(cmd, third, "Chained call should return same instance");
        assertSame(first, second, "All returned instances should be identical");
    }

    @Test
    @DisplayName("Should work with complex nested data")
    void testComplexCommandData() {
        ComplexCommand cmd = new ComplexCommand("MyCommand", 42, true);

        ComplexCommand result = cmd.getCommand();

        assertEquals("MyCommand", result.getName(), "Name should be preserved");
        assertEquals(42, result.getCount(), "Count should be preserved");
        assertTrue(result.getEnabled(), "Enabled flag should be preserved");
        assertSame(cmd, result, "Should be same instance");
    }

    @Test
    @DisplayName("Should support different generic types")
    void testDifferentGenericTypes() {
        StringCommand stringCmd = new StringCommand();
        IntegerCommand intCmd = new IntegerCommand();

        // Both should work independently with their types
        StringCommand stringResult = stringCmd.getCommand();
        IntegerCommand intResult = intCmd.getCommand();

        assertNotNull(stringResult, "String command result should not be null");
        assertNotNull(intResult, "Integer command result should not be null");

        assertTrue(stringResult instanceof StringCommand, "Should return StringCommand instance");
        assertTrue(intResult instanceof IntegerCommand, "Should return IntegerCommand instance");

        assertNotSame(stringResult, intResult, "Different command types should be different instances");
    }

    @Test
    @DisplayName("Should verify type safety with generics")
    void testGenericTypeSafety() {
        StringCommand cmd = new StringCommand();

        // This demonstrates type safety - getCommand returns the correct type
        StringCommand result = cmd.getCommand();

        // No casting needed - type is preserved
        assertEquals("test", result.getValue(), "Type-safe access to StringCommand methods");

        // Verify runtime type
        assertEquals(StringCommand.class, result.getClass(), "Runtime type should match");
    }

    @Test
    @DisplayName("Should handle null values in command state")
    void testNullValueHandling() {
        StringCommand cmd = new StringCommand();
        cmd.setValue(null);

        StringCommand result = cmd.getCommand();

        assertNull(result.getValue(), "Null value should be preserved");
        assertSame(cmd, result, "Should still return same instance");
    }

    @Test
    @DisplayName("Should verify concrete implementations follow contract")
    void testConcreteImplementationsContract() {
        // Test that actual command classes from the binding follow the same pattern
        Brightness brightness = new Brightness();
        Brightness brightnessResult = brightness.getCommand();
        assertSame(brightness, brightnessResult, "Brightness should return itself");

        Color color = new Color();
        Color colorResult = color.getCommand();
        assertSame(color, colorResult, "Color should return itself");

        Mode mode = new Mode();
        Mode modeResult = mode.getCommand();
        assertSame(mode, modeResult, "Mode should return itself");
    }

    @Test
    @DisplayName("Should work with command modification after retrieval")
    void testCommandModificationAfterRetrieval() {
        IntegerCommand cmd = new IntegerCommand();
        cmd.setValue(50);

        IntegerCommand retrieved = cmd.getCommand();
        assertEquals(50, retrieved.getValue(), "Initial value should be 50");

        // Modify through retrieved reference
        retrieved.setValue(75);

        // Original should be modified too (same instance)
        assertEquals(75, cmd.getValue(), "Original command should be modified");
        assertEquals(75, retrieved.getValue(), "Retrieved command should show modification");
    }

    @Test
    @DisplayName("Should support immutability pattern if needed")
    void testImmutabilityPattern() {
        ComplexCommand cmd = new ComplexCommand("ImmutableTest", 100, false);

        ComplexCommand result = cmd.getCommand();

        // Verify data is accessible
        assertEquals("ImmutableTest", result.getName());
        assertEquals(100, result.getCount());
        assertFalse(result.getEnabled());

        // Since it's the same instance, changes would affect both
        // This test verifies the pattern supports both mutable and immutable designs
        assertSame(cmd, result, "Should be same instance");
    }

    @Test
    @DisplayName("Should work consistently across different instances")
    void testConsistencyAcrossInstances() {
        StringCommand cmd1 = new StringCommand();
        cmd1.setValue("command1");

        StringCommand cmd2 = new StringCommand();
        cmd2.setValue("command2");

        StringCommand result1 = cmd1.getCommand();
        StringCommand result2 = cmd2.getCommand();

        // Each instance should return itself
        assertSame(cmd1, result1, "First command should return itself");
        assertSame(cmd2, result2, "Second command should return itself");

        // But they should be different from each other
        assertNotSame(result1, result2, "Different commands should remain different");
        assertEquals("command1", result1.getValue());
        assertEquals("command2", result2.getValue());
    }
}
