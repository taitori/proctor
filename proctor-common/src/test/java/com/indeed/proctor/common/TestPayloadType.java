package com.indeed.proctor.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.indeed.proctor.common.model.Payload;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.indeed.proctor.common.PayloadType.payloadTypeForValue;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class TestPayloadType {
    @Test
    public void testPayloadTypeNames() {
        final List<String> names = PayloadType.allTypeNames();
        assertEquals(7, names.size());

        for (final String s : new String[]{
                "doubleValue", "doubleArray",
                "longValue", "longArray",
                "stringValue", "stringArray",
                "map"
        }) {
            final PayloadType p = PayloadType.payloadTypeForName(s);
            assertEquals(s, p.payloadTypeName);
        }
        // Verify that trying to get the PayloadType for a nonsense
        // name throws an exception.
        assertThatThrownBy(() -> PayloadType.payloadTypeForName("gruntleBuggy"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testPayloadTypeForValueRetrieval_DoubleArrayType() {
        assertEquals(PayloadType.DOUBLE_ARRAY, payloadTypeForValue(singletonList(1.1D)));
        assertNotEquals(PayloadType.DOUBLE_ARRAY, payloadTypeForValue(singletonList(100L)));
        assertEquals(PayloadType.DOUBLE_ARRAY, payloadTypeForValue(singletonList(1.2F)));
        assertEquals(PayloadType.DOUBLE_ARRAY, payloadTypeForValue(new Double[]{1.0D, 2.0D}));
        assertEquals(PayloadType.DOUBLE_ARRAY, payloadTypeForValue(new Float[]{1.0F, 2.0F}));
        assertNotEquals(PayloadType.DOUBLE_ARRAY, payloadTypeForValue(new Long[]{100L, 200L}));
    }

    @Test
    public void testPayloadTypeForValueRetrieval_LongArrayType() {
        assertEquals(PayloadType.LONG_ARRAY, payloadTypeForValue(singletonList(11L)));
        assertEquals(PayloadType.LONG_ARRAY, payloadTypeForValue(singletonList(11)));
        assertNotEquals(PayloadType.LONG_ARRAY, payloadTypeForValue(singletonList(1.1D)));
        assertEquals(PayloadType.LONG_ARRAY, payloadTypeForValue(new Long[]{100L, 200L}));
        assertEquals(PayloadType.LONG_ARRAY, payloadTypeForValue(new Integer[]{10, 20}));
        assertNotEquals(PayloadType.LONG_ARRAY, payloadTypeForValue(new Double[]{1.0D, 2.0D}));
    }

    @Test
    public void testPayloadTypeForValueRetrieval_StringArrayType() {
        assertEquals(PayloadType.STRING_ARRAY, payloadTypeForValue(singletonList("Ya")));
        assertNotEquals(PayloadType.STRING_ARRAY, payloadTypeForValue(singletonList(100)));
        assertEquals(PayloadType.STRING_ARRAY, payloadTypeForValue(new String[]{"yea", "Ya"}));
        assertNotEquals(PayloadType.STRING_ARRAY, payloadTypeForValue(new Float[]{1.0F, 2.0F}));
    }

    @Test
    public void testPayloadTypeForValueRetrieval_NonArrayTypes() {
        assertEquals(PayloadType.MAP, payloadTypeForValue(ImmutableMap.of("string", "string")));
        assertEquals(PayloadType.LONG_VALUE, payloadTypeForValue(100));
        assertEquals(PayloadType.LONG_VALUE, payloadTypeForValue(100L));
        assertEquals(PayloadType.DOUBLE_VALUE, payloadTypeForValue(1.1D));
        assertEquals(PayloadType.DOUBLE_VALUE, payloadTypeForValue(2.1F));
        assertEquals(PayloadType.STRING_VALUE, payloadTypeForValue("yes"));
    }

    @Test
    public void testUnknownPayloadTypeForValue() {
        assertThatThrownBy(() -> payloadTypeForValue(emptyList()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> payloadTypeForValue(PayloadType.DOUBLE_VALUE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testPayloadHasType() {
        final Map<PayloadType, Payload> inputs = getPayloadTypePayloadSampleMap();

        inputs.forEach((type, payload) -> {
            inputs.keySet().forEach(type2 -> {
                if (type == type2) {
                    assertTrue(payload + " should have type " + type2, type2.payloadHasThisType(payload));
                    assertEquals(payload.fetchType(), type2.payloadTypeName);
                } else {
                    assertFalse(payload + " should not have type " + type2, type2.payloadHasThisType(payload));
                }
            });
        });
    }

    private static Map<PayloadType, Payload> getPayloadTypePayloadSampleMap() {
        final ImmutableList<Consumer<Payload>> map = ImmutableList.<Consumer<Payload>>builder()
                .add(p -> p.setDoubleValue(47.0D))
                .add(p -> p.setDoubleArray(new Double[]{98.0D, -137D}))
                .add(p -> p.setLongValue(42L))
                .add(p -> p.setLongArray(new Long[]{9L, 8L, 7L, 6L, 5L, 4L}))
                .add(p -> p.setStringValue("foobar"))
                .add(p -> p.setStringArray(new String[]{"foo", "bar", "baz"}))
                .add(p -> p.setMap(ImmutableMap.of("a", 1.0D, "b", new Double[]{57.0D, -8.0D, 79.97D}, "c", "somevals")))
                .build();
        return map.stream()
                .map(consumer -> {
                    final Payload p = new Payload();
                    consumer.accept(p);
                    return p;
                })
                .collect(Collectors.toMap(
                        e -> PayloadType.payloadTypeForName(e.fetchType()), e -> e));
    }

}