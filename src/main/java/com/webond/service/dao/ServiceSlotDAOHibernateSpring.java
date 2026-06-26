package com.webond.service.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import com.webond.service.model.ServiceSlotVO;

@Repository
public class ServiceSlotDAOHibernateSpring implements ServiceSlotDAO_interface {

    private final SessionFactory factory;

    public ServiceSlotDAOHibernateSpring(SessionFactory factory) {
        this.factory = factory;
    }

    private Session getSession() {
        return factory.getCurrentSession();
    }

    @Override
    public void insert(ServiceSlotVO svcS) {
        getSession().persist(svcS);
    }

    @Override
    public void update(ServiceSlotVO svcS) {
        getSession().merge(svcS);
    }

    @Override
    public void delete(Integer serviceSlotId) {
        ServiceSlotVO svcS = getSession().find(ServiceSlotVO.class, serviceSlotId);

        if (svcS != null) {
            getSession().remove(svcS);
        }
    }

    @Override
    public ServiceSlotVO getOne(Integer serviceSlotId) {
        return getSession()
                .createQuery(
                    "select ss from ServiceSlotVO ss " +
                    "left join fetch ss.service " +
                    "where ss.serviceSlotId = :serviceSlotId",
                    ServiceSlotVO.class
                )
                .setParameter("serviceSlotId", serviceSlotId)
                .uniqueResult();
    }

    @Override
    public List<ServiceSlotVO> getAll() {
        return getSession()
                .createQuery(
                    "select ss from ServiceSlotVO ss " +
                    "left join fetch ss.service " +
                    "order by ss.serviceSlotId",
                    ServiceSlotVO.class
                )
                .getResultList();
    }

    @Override
    public List<ServiceSlotVO> getByServiceId(Integer serviceId) {
        return getSession()
                .createQuery(
                    "select ss from ServiceSlotVO ss " +
                    "left join fetch ss.service " +
                    "where ss.serviceId = :serviceId " +
                    "order by ss.startTime",
                    ServiceSlotVO.class
                )
                .setParameter("serviceId", serviceId)
                .getResultList();
    }
}