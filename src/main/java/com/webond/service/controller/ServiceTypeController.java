package com.webond.service.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.service.model.ServiceTypeService;
import com.webond.service.model.ServiceTypeVO;

@Controller
@RequestMapping("/service-types")
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeSvc;

    public ServiceTypeController(ServiceTypeService serviceTypeSvc) {
        this.serviceTypeSvc = serviceTypeSvc;
    }

    // 查全部服務類型
    // URL: GET /service-types
    @GetMapping
    public String getAll(Model model) {
        List<ServiceTypeVO> serviceTypeList = serviceTypeSvc.getAll();

        model.addAttribute("serviceTypeList", serviceTypeList);

        return "back-end/service/service-type-list";
    }

    // 查單一服務類型
    // URL: GET /service-types/{serviceTypeId}
    @GetMapping("/{serviceTypeId}")
    public String getOne(@PathVariable Integer serviceTypeId, Model model) {
        ServiceTypeVO serviceTypeVO = serviceTypeSvc.findByPK(serviceTypeId);

        if (serviceTypeVO == null) {
            model.addAttribute("errorMsg", "查無此服務類型");
            model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());

            return "back-end/service/service-type-list";
        }

        model.addAttribute("serviceTypeVO", serviceTypeVO);

        return "back-end/service/service-type-detail";
    }

    // 前往新增頁面
    // URL: GET /service-types/new
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("serviceTypeVO", new ServiceTypeVO());
        model.addAttribute("mode", "add");

        return "back-end/service/service-type-form";
    }

    // 新增服務類型
    // URL: POST /service-types
    @PostMapping
    public String insert(
            @ModelAttribute ServiceTypeVO serviceTypeVO,
            Model model,
            RedirectAttributes redirectAttributes) {

        Map<String, String> errors = validateServiceType(serviceTypeVO);

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("serviceTypeVO", serviceTypeVO);
            model.addAttribute("mode", "add");

            return "back-end/service/service-type-form";
        }

        serviceTypeSvc.add(
                serviceTypeVO.getTypeName().trim(),
                serviceTypeVO.getDescrip().trim(),
                serviceTypeVO.getTypeMode(),
                serviceTypeVO.getImgURL().trim()
        );

        redirectAttributes.addFlashAttribute("successMsg", "新增服務類型成功");

        return "redirect:/service-types";
    }

    // 前往修改頁面
    // URL: GET /service-types/{serviceTypeId}/edit
    @GetMapping("/{serviceTypeId}/edit")
    public String showUpdateForm(@PathVariable Integer serviceTypeId, Model model) {
        ServiceTypeVO serviceTypeVO = serviceTypeSvc.findByPK(serviceTypeId);

        if (serviceTypeVO == null) {
            model.addAttribute("errorMsg", "查無此服務類型，無法修改");
            model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());

            return "back-end/service/service-type-list";
        }

        model.addAttribute("serviceTypeVO", serviceTypeVO);
        model.addAttribute("mode", "edit");

        return "back-end/service/service-type-form";
    }

    // 修改服務類型
    // URL: POST /service-types/{serviceTypeId}/edit
    @PostMapping("/{serviceTypeId}/edit")
    public String update(
            @PathVariable Integer serviceTypeId,
            @ModelAttribute ServiceTypeVO serviceTypeVO,
            Model model,
            RedirectAttributes redirectAttributes) {

        Map<String, String> errors = validateServiceType(serviceTypeVO);

        if (!errors.isEmpty()) {
            serviceTypeVO.setSvcTypeID(serviceTypeId);

            model.addAttribute("errors", errors);
            model.addAttribute("serviceTypeVO", serviceTypeVO);
            model.addAttribute("mode", "edit");

            return "back-end/service/service-type-form";
        }

        ServiceTypeVO oldVO = serviceTypeSvc.findByPK(serviceTypeId);

        if (oldVO == null) {
            model.addAttribute("errorMsg", "查無此服務類型，無法修改");
            model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());

            return "back-end/service/service-type-list";
        }

        serviceTypeSvc.update(
                serviceTypeId,
                serviceTypeVO.getTypeName().trim(),
                serviceTypeVO.getDescrip().trim(),
                serviceTypeVO.getTypeMode(),
                serviceTypeVO.getImgURL().trim()
        );

        redirectAttributes.addFlashAttribute("successMsg", "修改服務類型成功");

        return "redirect:/service-types/" + serviceTypeId;
    }

    // 刪除服務類型
    // URL: POST /service-types/{serviceTypeId}/delete
    @PostMapping("/{serviceTypeId}/delete")
    public String delete(
            @PathVariable Integer serviceTypeId,
            RedirectAttributes redirectAttributes) {

        ServiceTypeVO oldVO = serviceTypeSvc.findByPK(serviceTypeId);

        if (oldVO == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "查無此服務類型，無法刪除");
            return "redirect:/service-types";
        }

        serviceTypeSvc.delete(serviceTypeId);

        redirectAttributes.addFlashAttribute("successMsg", "刪除服務類型成功");

        return "redirect:/service-types";
    }

    private Map<String, String> validateServiceType(ServiceTypeVO vo) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (vo == null) {
            errors.put("request", "請提供服務類型資料");
            return errors;
        }

        if (vo.getTypeName() == null || vo.getTypeName().trim().isEmpty()) {
            errors.put("typeName", "服務類型名稱請勿空白");
        } else if (vo.getTypeName().trim().length() > 50) {
            errors.put("typeName", "服務類型名稱不可超過 50 字");
        }

        if (vo.getDescrip() == null || vo.getDescrip().trim().isEmpty()) {
            errors.put("descrip", "服務類型描述請勿空白");
        } else if (vo.getDescrip().trim().length() > 255) {
            errors.put("descrip", "服務類型描述不可超過 255 字");
        }

        if (vo.getTypeMode() == null) {
            errors.put("typeMode", "請選擇服務類型模式");
        } else if (vo.getTypeMode() != 0 && vo.getTypeMode() != 1) {
            errors.put("typeMode", "類型模式只能是 0 或 1");
        }

        if (vo.getImgURL() == null || vo.getImgURL().trim().isEmpty()) {
            errors.put("imgURL", "圖片路徑請勿空白");
        }

        return errors;
    }
}