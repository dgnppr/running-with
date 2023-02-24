package com.runningwith.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningwith.study.form.StudyDescriptionForm;
import com.runningwith.tag.TagEntity;
import com.runningwith.tag.TagForm;
import com.runningwith.tag.TagRepository;
import com.runningwith.tag.TagService;
import com.runningwith.users.CurrentUser;
import com.runningwith.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.runningwith.study.StudyController.URL_STUDY_PATH;
import static com.runningwith.utils.CustomStringUtils.getEncodedUrl;
import static com.runningwith.utils.WebUtils.REDIRECT;

@Slf4j
@Controller
@RequestMapping(URL_STUDY_PATH + "{path}")
@RequiredArgsConstructor
public class StudySettingsController {

    public static final String VIEW_STUDY_SETTINGS_DESCRIPTION = "study/settings/description";
    public static final String URL_STUDY_SETTINGS_DESCRIPTION = "/settings/description";
    public static final String STUDY_DESCRIPTION_FORM = "studyDescriptionForm";
    public static final String URL_STUDY_SETTINGS_BANNER = "/settings/banner";
    public static final String VIEW_STUDY_SETTINGS_BANNER = "study/settings/banner";
    public static final String URL_SETTINGS_BANNER_ENABLE = "/settings/banner/enable";
    public static final String URL_SETTINGS_BANNER_DISABLE = "/settings/banner/disable";
    public static final String VIEW_STUDY_SETTINGS_TAGS = "study/settings/tags";
    public static final String URL_STUDY_TAGS = "/settings/tags";
    public static final String URL_STUDY_TAGS_ADD = "/settings/tags/add";
    public static final String URL_STUDY_TAGS_REMOVE = "/settings/tags/remove";
    private final TagRepository tagRepository;
    private final StudyService studyService;
    private final TagService tagService;
    private final ObjectMapper objectMapper;

    // TODO 뷰 수정(DRAFT OFF)
    @GetMapping(URL_STUDY_SETTINGS_DESCRIPTION)
    public String studyDescriptionSettingView(@CurrentUser UsersEntity usersEntity, @PathVariable String path, Model model) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        model.addAttribute("user", usersEntity);
        model.addAttribute("study", studyEntity);
        model.addAttribute(new StudyDescriptionForm(studyEntity));
        return VIEW_STUDY_SETTINGS_DESCRIPTION;
    }

    @PostMapping(URL_STUDY_SETTINGS_DESCRIPTION)
    public String updateStudyDescription(@CurrentUser UsersEntity usersEntity, @PathVariable String path,
                                         @Validated StudyDescriptionForm studyDescriptionForm, BindingResult bindingResult,
                                         Model model, RedirectAttributes attributes) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", usersEntity);
            model.addAttribute("study", studyEntity);
            return VIEW_STUDY_SETTINGS_DESCRIPTION;
        }

        studyService.updateStudyDescription(studyEntity, studyDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개 수정 완료");
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(studyEntity.getPath()) + URL_STUDY_SETTINGS_DESCRIPTION;
    }

    @GetMapping(URL_STUDY_SETTINGS_BANNER)
    public String studyBannerSettingView(@CurrentUser UsersEntity usersEntity, @PathVariable String path, Model model) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        model.addAttribute("user", usersEntity);
        model.addAttribute("study", studyEntity);
        return VIEW_STUDY_SETTINGS_BANNER;
    }

    @PostMapping(URL_STUDY_SETTINGS_BANNER)
    public String updateStudyBannerImage(@CurrentUser UsersEntity usersEntity, @PathVariable String path,
                                         String image, RedirectAttributes attributes) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        studyService.updateStudyBannerImage(studyEntity, image);
        attributes.addFlashAttribute("message", "배너 이미지 수정 완료");
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_SETTINGS_BANNER;
    }

    @PostMapping(URL_SETTINGS_BANNER_ENABLE)
    public String enableStudyBannerImage(@CurrentUser UsersEntity usersEntity, @PathVariable String path) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        studyService.enableStudyBanner(studyEntity);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_SETTINGS_BANNER;
    }

    @PostMapping(URL_SETTINGS_BANNER_DISABLE)
    public String unableStudyBannerImage(@CurrentUser UsersEntity usersEntity, @PathVariable String path) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        studyService.disableStudyBanner(studyEntity);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_SETTINGS_BANNER;
    }

    @GetMapping(URL_STUDY_TAGS)
    public String studyTagsUpdateView(@CurrentUser UsersEntity usersEntity, @PathVariable String path, Model model) throws JsonProcessingException {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);

        model.addAttribute("user", usersEntity);
        model.addAttribute("study", studyEntity);

        List<String> tags = studyService.getStudyTags(studyEntity).stream().map(TagEntity::getTitle).collect(Collectors.toList());
        List<String> whitelist = tagRepository.findAll().stream().map(TagEntity::getTitle).collect(Collectors.toList());

        model.addAttribute("tags", tags);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(whitelist));

        return VIEW_STUDY_SETTINGS_TAGS;
    }

    @PostMapping(URL_STUDY_TAGS_ADD)
    @ResponseBody
    public ResponseEntity addStudyTags(@CurrentUser UsersEntity usersEntity, @PathVariable String path
            , @RequestBody TagForm tagForm) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        TagEntity tagEntity = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(studyEntity, tagEntity);
        return ResponseEntity.ok().build();
    }

    @PostMapping(URL_STUDY_TAGS_REMOVE)
    @ResponseBody
    public ResponseEntity removeStudyTags(@CurrentUser UsersEntity usersEntity, @PathVariable String path
            , @RequestBody TagForm tagForm) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        Optional<TagEntity> optionalTagEntity = tagRepository.findByTitle(tagForm.getTagTitle());
        if (optionalTagEntity.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        TagEntity tagEntity = optionalTagEntity.get();
        studyService.removeTag(studyEntity, tagEntity);
        return ResponseEntity.ok().build();
    }

}
