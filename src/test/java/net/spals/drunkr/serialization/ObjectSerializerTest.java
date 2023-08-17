package net.spals.drunkr.serialization;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMap;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.model.Person;
import net.spals.drunkr.model.Persons;

/**
 * Tests for {@link ObjectSerializer}.
 *
 * @author spags
 */
public class ObjectSerializerTest {

    private ObjectSerializer serializer;

    @BeforeMethod
    public void setUp() {
        serializer = new ObjectSerializer(ObjectMappers.jerseyMapper());
    }

    @Test
    public void patchUser() {
        final Person person = serializer.patch(Persons.SPAGS, ImmutableMap.of("weight", 180), Person.class);

        assertThat(person.weight()).isEqualTo(180.0);
    }
}