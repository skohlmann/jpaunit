# jpaunit

The idea is simple: forget about the database. Set up and expect database state using your JPA entities.

## With plain JUnit

### Code

```
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.zimory.jpaunit.core.context.JpaUnitConfig;
import com.zimory.jpaunit.core.junit.JpaUnitRule;
import com.zimory.jpaunit.core.annotation.ShouldMatchJpaDataSet;
import com.zimory.jpaunit.core.annotation.UsingJpaDataSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class MyAwesomeTest {

    private static EntityManagerFactory emf;

    @BeforeClass
    public static void setUpClass() {
        emf = Persistence.createEntityManagerFactory("test");
    }

    @AfterClass
    public static void tearDownClass() {
        emf.close();
    }
    
    @Rule
    public JpaUnitRule jpaUnitRule = new JpaUnitRule(
        Suppliers.ofInstance(new JpaUnitConfig()), 
        new Supplier<EntityManagerFactory>() {
            @Override
            public EntityManagerFactory get() {
                return emf;
            }
        });

    private EntityManager em;

    @Before
    public void setUp() {
        em = emf.createEntityManager();
    }

    @After
    public void tearDown() {
        em.close();
    }

    @Test
    @UsingJpaDataSet
    @ShouldMatchJpaDataSet
    public void findAndPersist() {
        em.getTransaction().begin();

        final SomeEntity e0 = em.find(SomeEntity.class, new UUID(0, 0));

        assertThat(e0, notNullValue());
        assertThat(e0.getName(), equalTo("old MacDonald had a farm"));

        final SomeEntity e1 = new SomeEntity();
        e1.setId(new UUID(0, 1));
        e1.setName("E-I-E-I-O");

        em.persist(e1);
        em.getTransaction().commit();
    }
    
}
    
```

### Datasets
#### /datasets/MyAwesomeTest/findAndPersist.yml

```
--- &association1 !Association
id: 00000000-0000-0000-0000-000000000000
--- !SomeEntity
id: 00000000-0000-0000-0000-000000000000
name: old MacDonald had a farm
associations:
  - *association1
```

#### /datasets/MyAwesomeTest/expected-findAndPersist.yml

```
--- &association1 !Association
id: 00000000-0000-0000-0000-000000000000
--- !SomeEntity
id: 00000000-0000-0000-0000-000000000000
name: old MacDonald had a farm
associations:
  - *association1
--- !SomeEntity
id: 00000000-0000-0000-0000-000000000001
name: E-I-E-I-O
associations: []
```


## With Spring Tests
```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        JpaUnitTestExecutionListener.class
})
public class MyAwesomeTest {
    // don't need the @Rule, since we are using JpaUnitTestExecutionListener here
}
```

In case of Spring Tests, jpaunit expects an instance of JpaUnitConfig and JPA EntityManagerFactory to be present in the Spring context.

## Datasets

### Location

Datasets are always looked up in the "/datasets" directory (relative to the classpath). 

- if the ```value()``` attribute of the ```@UsingJpaDataSet``` or ```@ShouldMatchJpaDataSet``` is specified, then the value of the attribute is taken as a path relative to the said default dataset directory.
- when not specified, the relative path is constructed from the names of the class and the method the annotation is applied to:
  - ```@UsingJpaDataSet```: "ClassName/methodName"
  - ```@ShouldMatchJpaDataSet```: "ClassName/expected-methodName"
- the extension ".yml" is implied (i.e if omitted, it's appended to the path)
 
### Content

Datasets are written in YAML, as shown above. All rules apply (referencing other entities is possible using YAML syntax, etc.).

Entity types are matched based on the YAML tags, which can reference either the full-blown classname of the entity or the often shorter JPA entity name (e.g. ```!com.acme.Entity``` vs ```!Entity```).

## Custom serializers

Often one might find in need of writing custom serializers for scalar types (the more common types are supported out of the box, thanks to the [YamlBeans library](http://yamlbeans.sourceforge.net/), but custom serializers can be added simply by adding them to the ```JpaUnitConfig``` instance that is being passed to jpaunit.

```
import java.util.UUID;

import com.zimory.jpaunit.core.serialization.TypedScalarSerializer;

public class UuidSerializer extends TypedScalarSerializer<UUID> {

    public UuidSerializer() {
        super(UUID.class);
    }

    @Override
    public String write(final UUID o) {
        return o.toString();
    }

    @Override
    public UUID read(final String s) {
        return UUID.fromString(s);
    }

}

final JpaUnitConfig config = new JpaUnitConfig();
config.setCustomSerializers(ImmutableList.of(new UuidSerializer()));
```

_(actually, the UuidSerializer above is registered by default by jpaunit in addition to all those available from YamlBeans)_

This config can later be fed to the JpaUnitRule via the constructor if using pure JUnit. 

If using Spring Tests, one simply needs to register a single instance of the JpaUnitConfig class in the associated Spring context (the one configured via the ```@org.springframework.test.context.ContextConfiguration``` annotation) and set the list of serializers into that config.
