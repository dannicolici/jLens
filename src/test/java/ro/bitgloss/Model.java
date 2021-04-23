package ro.bitgloss;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Data class Street { String name; Integer number; }
@Data @Builder class Address { String city; Street street; }
@Data class Company { String name; Address address; }
@Data class Person { String name; Address address; List<Company> workHistory; }

public class Model {

    public static void main(String[] args) {
        // Define properties (basically by specifying the getter/setter from the data structure)
        Property<Person, String> personName = Property.of(Person::getName, Person::setName);
        Property<Person, Address> personAddress = Property.of(Person::getAddress, Person::setAddress);
        Property<Address, Street> addressStreet = Property.of(Address::getStreet, Address::setStreet);
        Property<Street, String> streetName = Property.of(Street::getName, Street::setName);
        Property<Person, List<Company>> workHistory = Property.of(Person::getWorkHistory, Person::setWorkHistory);
        Property<Person, String> personStreetName = personAddress.to(addressStreet).to(streetName);

        // Define (lazy) values
        PropertyOperations<Person> propertyOperations = new PropertyOperations<>();
        propertyOperations.set(personName, "Jim");
        propertyOperations.set(personAddress, Address.builder().street(new Street()).build());
        propertyOperations.set(workHistory, singletonList(new Company()));
        propertyOperations.set(personStreetName, "my street");

        // Define (lazy) operations
        propertyOperations.transform(personName, asList(String::toUpperCase, s -> new StringBuilder(s).reverse().toString()));
        propertyOperations.transform(personStreetName, String::toUpperCase);

        // Apply propertyOperations on data structure
        propertyOperations.applyWithHook(new Person(), System.out::println);
    }

}
