package com.webond.service.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.webond.service.model.ServiceVO;

//HibernateSpring版
@Repository
public class ServiceDAOHibernateSpring implements ServiceDAO_interface {

    private final SessionFactory factory;
    
    @Autowired
    public ServiceDAOHibernateSpring(SessionFactory factory) {
        this.factory = factory;
    }

    private Session getSession() {
        return factory.getCurrentSession();
    }

    @Override
    public void insert(ServiceVO svc) {
        getSession().persist(svc);
    }

    @Override
    public void delete(Integer PK) {
        ServiceVO svc = getSession().find(ServiceVO.class, PK);

        if (svc != null) {
            getSession().remove(svc);
        }
    }

    @Override
    public void update(ServiceVO svc) {
        getSession().merge(svc);
    }

    @Override
    public ServiceVO getOne(Integer PK) {
        return getSession().find(ServiceVO.class, PK);
    }

    @Override
    public List<ServiceVO> getAll() {
        return getSession()
                .createQuery("from ServiceVO", ServiceVO.class)
                .getResultList();
    }

    @Override
    public List<ServiceVO> getByServiceTypeId(Integer serviceTypeId) {
        return getSession()
                .createQuery(
                    "select s from ServiceVO s " +
                    "join fetch s.serviceType st " +
                    "where st.svcTypeID = :serviceTypeId",
                    ServiceVO.class
                )
                .setParameter("serviceTypeId", serviceTypeId)
                .getResultList();
    }
}