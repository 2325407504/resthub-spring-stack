package org.resthub.core.test.service;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.resthub.core.service.GenericService;
import org.resthub.core.test.AbstractResthubTest;
import org.resthub.core.test.AbstractResthubTransactionAwareTest;
import org.resthub.core.util.ClassUtils;
import org.resthub.core.util.MetamodelUtils;

public abstract class AbstractServiceTest<T, PK extends Serializable, D extends GenericService<T, PK>> extends AbstractResthubTransactionAwareTest {

    // -----------------------------------------------------------------------------------------------------------------
    // Attributes
    /**
     * The tested Service
     */
    protected D service;
    /**
     * The tested POJO
     */
    protected PK id;
    
    @PersistenceContext
    private EntityManager em;

    // -----------------------------------------------------------------------------------------------------------------
    // Property
    /**
     * Injection of Service.
     */
    public void setService(D service) {
        this.service = service;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Protected methods
    /**
     * Automatically retrieve ID from entity instance.
     * 
     * @param obj The object from whom we need primary key
     * @return The corresponding primary key.
     */
    protected PK getIdFromEntity(T obj) {
        MetamodelUtils utils = new MetamodelUtils<T, PK>((Class<T>) ClassUtils.getGenericTypeFromBean(this.service), em.getMetamodel());
        return (PK) utils.getIdFromEntity(obj);
    }
    
    @SuppressWarnings("unchecked")
    protected T createTestRessource() throws Exception {
        return (T) ClassUtils.getGenericTypeFromBean(this.service).newInstance();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Setup/finalize
    @Before
    public void setUp() throws Exception {
        T resource = service.create(this.createTestRessource());
        this.id = getIdFromEntity(resource);
    }
    
    @After
    public void tearDown() throws Exception {
    	// Don't use deleteAll because it does not acheive cascade delete
    	for (T resource : service.findAll()) {
    		service.delete(resource);
        }

    }

    // -----------------------------------------------------------------------------------------------------------------
    // Tests methods
    @Test
    public void testCreate() throws Exception {
        T resource = service.create(this.createTestRessource());

        T foundResource = service.findById(getIdFromEntity(resource));
        Assert.assertNotNull("Resource not created!", foundResource);
    }

    @Test
    public abstract void testUpdate() throws Exception;

    @Test
    public void testDelete() throws Exception {
        T resource = service.findById(this.id);
        service.delete(resource);

        T foundResource = service.findById(this.id);
        Assert.assertNull("Resource not deleted!", foundResource);
    }

    @Test
    public void testDeleteById() throws Exception {
        T resource = service.findById(this.id);
        service.delete(getIdFromEntity(resource));

        T foundResource = service.findById(this.id);
        Assert.assertNull("Resource not deleted!", foundResource);
    }

    @Test
    public void testFindById() throws Exception {
        T resource = service.findById(this.id);
        Assert.assertNotNull("Resource should not be null!", resource);
        Assert.assertEquals("Resource id and resourceId should be equals!", this.id, this.getIdFromEntity(resource));
    }

    @Test
    public void testFindAll() throws Exception {
        List<T> resourceList = service.findAll(null).asList();
        Assert.assertTrue("No resources found!", resourceList.size() >= 1);
    }

    @Test
    public void testCount() throws Exception {
        Long nb = service.count();
        Assert.assertTrue("No resources found!", nb >= 1);
    }
}
