package org.hibernate.metamodel.source.annotations.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.junit.Test;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.MultipleHiLoPerTableGenerator;
import org.hibernate.id.SequenceHiLoGenerator;
import org.hibernate.id.UUIDHexGenerator;
import org.hibernate.metamodel.MetadataSources;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.service.ServiceRegistryBuilder;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public class IdentifierGeneratorTest extends BaseAnnotationBindingTestCase {
	@Entity
	class NoGenerationEntity {
		@Id
		private long id;
	}

	@Test
	@Resources(annotatedClasses = NoGenerationEntity.class)
	public void testNoIdGeneration() {
		EntityBinding binding = getEntityBinding( NoGenerationEntity.class );
		IdentifierGenerator generator = binding.getHierarchyDetails().getEntityIdentifier().getIdentifierGenerator();
		assertNull( generator );
	}

	@Entity
	class AutoEntity {
		@Id
		@GeneratedValue
		private long id;

		public long getId() {
			return id;
		}
	}

	@Test
	@Resources(annotatedClasses = AutoEntity.class)
	public void testAutoGenerationType() {
		EntityBinding binding = getEntityBinding( AutoEntity.class );
		IdentifierGenerator generator = binding.getHierarchyDetails().getEntityIdentifier().getIdentifierGenerator();

		assertEquals( "Wrong generator", IdentityGenerator.class, generator.getClass() );
	}

	@Entity
	class TableEntity {
		@Id
		@GeneratedValue(strategy = GenerationType.TABLE)
		private long id;

		public long getId() {
			return id;
		}
	}

	@Test
	@Resources(annotatedClasses = TableEntity.class)
	public void testTableGenerationType() {
		EntityBinding binding = getEntityBinding( TableEntity.class );
		IdentifierGenerator generator = binding.getHierarchyDetails().getEntityIdentifier().getIdentifierGenerator();

		assertEquals( "Wrong generator", MultipleHiLoPerTableGenerator.class, generator.getClass() );
	}

	@Entity
	class SequenceEntity {
		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE)
		private long id;

		public long getId() {
			return id;
		}
	}

	@Test
	@Resources(annotatedClasses = SequenceEntity.class)
	public void testSequenceGenerationType() {
		EntityBinding binding = getEntityBinding( SequenceEntity.class );
		IdentifierGenerator generator = binding.getHierarchyDetails().getEntityIdentifier().getIdentifierGenerator();

		assertEquals( "Wrong generator", SequenceHiLoGenerator.class, generator.getClass() );
	}


	@Entity
	class NamedGeneratorEntity {
		@Id
		@GeneratedValue(generator = "my-generator")
		private long id;

		public long getId() {
			return id;
		}
	}

	@Test
	public void testUndefinedGenerator() {
		try {
			sources = new MetadataSources( new ServiceRegistryBuilder().buildServiceRegistry() );
			sources.addAnnotatedClass( NamedGeneratorEntity.class );
			sources.buildMetadata();
			fail();
		}
		catch ( MappingException e ) {
			assertTrue( e.getMessage().startsWith( "Unable to find named generator" ) );
		}
	}

	@Entity
	@GenericGenerator(name = "my-generator", strategy = "uuid")
	class NamedGeneratorEntity2 {
		@Id
		@GeneratedValue(generator = "my-generator")
		private long id;

		public long getId() {
			return id;
		}
	}

	@Test
	@Resources(annotatedClasses = NamedGeneratorEntity2.class)
	public void testNamedGenerator() {
		EntityBinding binding = getEntityBinding( NamedGeneratorEntity2.class );
		IdentifierGenerator generator = binding.getHierarchyDetails().getEntityIdentifier().getIdentifierGenerator();

		assertEquals( "Wrong generator", UUIDHexGenerator.class, generator.getClass() );
	}
}

