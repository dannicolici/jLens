package ro.bitgloss;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PropertyTest {

    @Test
    public void mutate() {
        Person person = new Person();
        Property<Person, String> personName = Property.of(Person::getName, Person::setName);

        personName.mutate(person, s -> "name");

        assertEquals("name", personName.get(person).get());
    }

    @Test
    public void to() {
        Person person = new Person();
        Property<Person, Address> personAddress = Property.of(Person::getAddress, Person::setAddress);
        Property<Address, Street> addressStreet = Property.of(Address::getStreet, Address::setStreet);
        Property<Street, String> streetName = Property.of(Street::getName, Street::setName);

        Property<Person, String> personStreetName = personAddress.to(addressStreet).to(streetName);

        personAddress.mutate(person, a -> Address.builder().street(new Street()).build());
        personStreetName.mutate(person, n -> "street");

        assertEquals("street", personStreetName.get(person).get());
    }
}