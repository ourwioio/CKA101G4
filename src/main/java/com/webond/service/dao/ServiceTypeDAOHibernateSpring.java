package com.webond.service.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.webond.service.model.ServiceTypeVO;

@Repository
public class ServiceTypeDAOHibernateSpring implements ServiceTypeDAO_interface {

    private final SessionFactory factory;

    @Autowired
    public ServiceTypeDAOHibernateSpring(SessionFactory factory) {
        this.factory = factory;
    }

    private Session getSession() {
        return factory.getCurrentSession();
    }

    @Override
    public void insert(ServiceTypeVO svcType) {
        getSession().persist(svcType);
    }

    @Override
    public void update(ServiceTypeVO svcType) {
        getSession().merge(svcType);
    }

    @Override
    public void delete(Integer PK) {
        ServiceTypeVO svcType = getSession().find(ServiceTypeVO.class, PK);

        if (svcType != null) {
            getSession().remove(svcType);
        }
    }

    @Override
    public ServiceTypeVO findByPK(Integer PK) {
        return getSession().find(ServiceTypeVO.class, PK);
    }

    @Override
    public List<ServiceTypeVO> getAll() {
        return getSession()
                .createQuery("from ServiceTypeVO", ServiceTypeVO.class)
                .getResultList();
    }

}