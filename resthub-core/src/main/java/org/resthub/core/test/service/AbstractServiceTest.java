package org.resthub.core.test.service;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.resthub.core.service.GenericService;
import org.resthub.core.test.AbstractTransactionAwareTest;
import org.resthub.core.util.ClassUtils;
import org.resthub.core.util.MetamodelUtils;

/**
 * Service test base class in order to test your service extending
 * GenericService<T, ID> This can be usefull to do a quick check that everything
 * is fine for basic CRUD functionnalities.
 * 
 * Best practices are to separate these kind of automatic tests from your custom
 * tests, that may use RESThub DbUnit integration, and that should implement the
 * tests you will write.
 * 
 * @param <T>
 *            The model class managed by the service
 * @param <ID>
 *            The ID class of the model class managed by the generic service
 * @param <S>
 *            Your generic service class
 */
public abstract class AbstractServiceTest<T, ID extends Serializable, S extends GenericService<T, ID>> extends
        AbstractTransactionAwareTest {

	protected static final Logger LOGGER = Logger.getLogger(AbstractServiceTest.class);
	
    /**
     * The tested Service
     */
    protected S service;

    protected ID id;

    private EntityManager em;
    
    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    /**
     * Injection of Service
     * @param service the service to set
     */
	public void setService(S service) {
		this.service = service;
	}

	/**
     * Automatically retrieve ID from entity instance.
     * 
     * @param obj
     *            The object from whom we need primary key
     * @return The corresponding primary key.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected ID getIdFromEntity(T obj) {
        MetamodelUtils utils = new MetamodelUtils<T, ID>((Class<T>) ClassUtils.getGenericTypeFromBean(this.service),
                em.getMetamodel());
        return (ID) utils.getIdFromEntity(obj);
    }

    @SuppressWarnings("unchecked")
    protected T createTestEntity() {
        try {
            return (T) ClassUtils.getGenericTypeFromBean(this.service).newInstance();
        } catch (Exception e) {
        	LOGGER.error("Error when creating the test entity : " + e);
            return null;
        }
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        T resource = service.create(this.createTestEntity());
        this.id = getIdFromEntity(resource);
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
        // Don't use deleteAll because it does not acheive cascade delete
        for (T resource : service.findAll()) {
            service.delete(resource);
        }
    }

    @Test
    public void testCreate() {
        T foundResource = service.findById(this.id);
        Assert.assertNotNull("Resource not created!", foundResource);
    }

    @Test
    public abstract void testUpdate();

    @Test
    public void testDelete() {
        T resource = service.findById(this.id);
        service.delete(resource);

        T foundResource = service.findById(this.id);
        Assert.assertNull("Resource not deleted!", foundResource);
    }

    @Test
    public void testDeleteById() {
        service.delete(this.id);
        T foundResource = service.findById(this.id);
        Assert.assertNull("Resource not deleted!", foundResource);
    }

    @Test
    public void testFindById() {
        T resource = service.findById(this.id);
        Assert.assertNotNull("Resource should not be null!", resource);
        Assert.assertEquals("Resource id and resourceId should be equals!", this.id, this.getIdFromEntity(resource));
    }

    @Test
    public void testFindAll() {
        List<T> resourceList = service.findAll();
        Assert.assertTrue("No resources found!", resourceList.size() >= 1);
    }

    @Test
    public void testCount() {
        Long nb = service.count();
        Assert.assertTrue("No resources found!", nb >= 1);
    }
}
