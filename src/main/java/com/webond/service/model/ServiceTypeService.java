package com.webond.service.model;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.service.repository.ServiceTypeRepository;

@Service
@Transactional
public class ServiceTypeService {

    // Spring Data JPA Repository
    // 取代原本的 ServiceTypeDAO_interface dao
    private final ServiceTypeRepository serviceTypeRepository;

    // 建構子注入
    // Spring 會自動把 ServiceTypeRepository 塞進來
    public ServiceTypeService(ServiceTypeRepository serviceTypeRepository) {
        this.serviceTypeRepository = serviceTypeRepository;
    }

    // 查單一服務類型
    @Transactional(readOnly = true)
    public ServiceTypeVO findByPK(Integer PK) {
        /*
         * 原本：
         * dao.findByPK(PK)
         *
         * Spring Data JPA：
         * findById(PK).orElse(null)
         */
        return serviceTypeRepository.findById(PK).orElse(null);
    }

    // 查全部服務類型
    @Transactional(readOnly = true)
    public List<ServiceTypeVO> getAll() {
        /*
         * 原本：
         * dao.getAll()
         *
         * Spring Data JPA：
         * findAll()
         */
        return serviceTypeRepository.findAll();
    }

    // 刪除服務類型
    public void delete(Integer PK) {
        /*
         * 先確認資料存在再刪，避免直接 deleteById 找不到資料時拋例外
         *
         * 注意：
         * 如果 SERVICE 表還有資料參照這個 SERVICE_TYPE_ID，
         * 資料庫可能會因為外鍵限制而不允許刪除。
         */
        if (serviceTypeRepository.existsById(PK)) {
            serviceTypeRepository.deleteById(PK);
        }
    }

    // 新增服務類型
    public ServiceTypeVO add(String name, String descrip, Integer mode, String URL) {

        // 可以保留基本驗證，避免髒資料進資料庫
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("服務類型名稱不可為空");
        }

        if (mode == null || (mode != 0 && mode != 1)) {
            throw new IllegalArgumentException("服務類型模式不合法");
        }

        ServiceTypeVO svcTVO = new ServiceTypeVO();

        svcTVO.setTypeName(name.trim());
        svcTVO.setDescrip(descrip);
        svcTVO.setTypeMode(mode);
        svcTVO.setImgURL(URL);

        /*
         * 原本：
         * dao.insert(svcTVO)
         *
         * Spring Data JPA：
         * save(svcTVO)
         *
         * 沒有 PK 時，save() 會做新增。
         */
        return serviceTypeRepository.save(svcTVO);
    }

    // 修改服務類型
    public ServiceTypeVO update(Integer PK, String name, String descrip, Integer mode, String URL) {

        /*
         * Spring Data JPA 比較建議：
         * 先查出原本資料
         * 再修改欄位
         * 最後 save()
         *
         * 不建議直接 new 一個 ServiceTypeVO 再 save，
         * 因為可能會覆蓋掉沒有帶進來的欄位。
         */
        ServiceTypeVO svcTVO = serviceTypeRepository.findById(PK).orElse(null);

        if (svcTVO == null) {
            return null;
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("服務類型名稱不可為空");
        }

        if (mode == null || (mode != 0 && mode != 1)) {
            throw new IllegalArgumentException("服務類型模式不合法");
        }

        svcTVO.setTypeName(name.trim());
        svcTVO.setDescrip(descrip);
        svcTVO.setTypeMode(mode);
        svcTVO.setImgURL(URL);

        /*
         * 有 PK 且資料已存在時，save() 會做更新。
         */
        return serviceTypeRepository.save(svcTVO);
    }
}