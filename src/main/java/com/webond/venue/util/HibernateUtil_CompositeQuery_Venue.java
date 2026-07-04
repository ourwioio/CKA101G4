package com.webond.venue.util;

import org.hibernate.Session;
import java.util.*;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.Query;

import com.webond.venue.model.VenueVO;

public class HibernateUtil_CompositeQuery_Venue {

    public static Predicate get_aPredicate_For_AnyDB(CriteriaBuilder builder, Root<VenueVO> root,
            String columnName, String value) {
        Predicate predicate = null;

        if ("venueId".equals(columnName))
            predicate = builder.equal(root.get(columnName), Integer.valueOf(value));
        else if ("venueName".equals(columnName) || "address".equals(columnName))
            predicate = builder.like(root.get(columnName), "%" + value + "%");
        else if ("venueStatus".equals(columnName))
            predicate = builder.equal(root.get(columnName), Byte.valueOf(value));
        else if ("capacityMin".equals(columnName))
            predicate = builder.greaterThanOrEqualTo(root.get("capacity"), Integer.valueOf(value));
        else if ("capacityMax".equals(columnName))
            predicate = builder.lessThanOrEqualTo(root.get("capacity"), Integer.valueOf(value));
        else if ("hourlyRateMin".equals(columnName))
            predicate = builder.greaterThanOrEqualTo(root.get("hourlyRate"), Integer.valueOf(value));
        else if ("hourlyRateMax".equals(columnName))
            predicate = builder.lessThanOrEqualTo(root.get("hourlyRate"), Integer.valueOf(value));
        else if ("venueTypeId".equals(columnName))
            predicate = builder.equal(root.get("venueTypeVO").get("venueTypeId"), Integer.valueOf(value));
        else if ("memberId".equals(columnName))
            predicate = builder.equal(root.get("member").get("memberId"), Integer.valueOf(value));

        return predicate;
    }

    @SuppressWarnings("unchecked")
    public static List<VenueVO> getAllC(Map<String, String[]> map, Session session) {
        List<VenueVO> list = null;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<VenueVO> criteriaQuery = builder.createQuery(VenueVO.class);
            Root<VenueVO> root = criteriaQuery.from(VenueVO.class);

            List<Predicate> predicateList = new ArrayList<Predicate>();

            // 前台複合查詢固定只查上架中的場地
            predicateList.add(builder.equal(root.get("venueStatus"), (byte) 1));

            Set<String> keys = map.keySet();
            int count = 0;
            for (String key : keys) {
                if (map.get(key) == null || map.get(key).length == 0) continue;
                String value = map.get(key)[0];
                if (value != null && value.trim().length() != 0 && !"action".equals(key)) {
                    count++;
                    Predicate p = get_aPredicate_For_AnyDB(builder, root, key, value.trim());
                    if (p != null) {
                        predicateList.add(p);
                    }
                    System.out.println("有送出查詢資料的欄位數count = " + count);
                }
            }
            System.out.println("predicateList.size()=" + predicateList.size());
            criteriaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
            criteriaQuery.orderBy(builder.asc(root.get("venueId")));

            Query query = session.createQuery(criteriaQuery);
            list = query.getResultList();
        } catch (RuntimeException ex) {
            throw ex;
        }
        return list;
    }
}