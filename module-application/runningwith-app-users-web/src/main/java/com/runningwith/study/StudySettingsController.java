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
import com.runningwith.users.form.ZoneForm;
import com.runningwith.zone.ZoneEntity;
import com.runningwith.zone.ZoneRepository;
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
import static com.runningwith.utils.WebUtils.URL_REDIRECT_ROOT;

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
    public static final String URL_STUDY_SETTINGS_BANNER_ENABLE = "/settings/banner/enable";
    public static final String URL_STUDY_SETTINGS_BANNER_DISABLE = "/settings/banner/disable";
    public static final String VIEW_STUDY_SETTINGS_TAGS = "study/settings/tags";
    public static final String URL_STUDY_SETTING_TAGS = "/settings/tags";
    public static final String URL_STUDY_SETTING_TAGS_ADD = "/settings/tags/add";
    public static final String URL_STUDY_SETTING_TAGS_REMOVE = "/settings/tags/remove";
    public static final String URL_STUDY_SETTING_ZONES = "/settings/zones";
    public static final String VIEW_STUDY_SETTINGS_ZONES = "study/settings/zones";
    public static final String URL_STUDY_SETTING_ZONES_REMOVE = "/settings/zones/remove";
    public static final String URL_STUDY_SETTING_ZONES_ADD = "/settings/zones/add";
    public static final String URL_STUDY_SETTING = "/settings/study";
    public static final String VIEW_STUDY_SETTINGS_STUDY = "study/settings/study";
    public static final String URL_STUDY_SETTINGS_PUBLISH = "/settings/study/publish";
    public static final String URL_STUDY_SETTINGS_CLOSE = "/settings/study/close";
    public static final String URL_STUDY_RECRUIT_START = "/settings/recruit/start";
    public static final String URL_STUDY_RECRUIT_STOP = "/settings/recruit/stop";
    public static final String URL_STUDY_SETTINGS_PATH = "/settings/study/path";
    public static final String URL_STUDY_SETTINGS_TITLE = "/settings/study/title";
    public static final String URL_STUDY_SETTINGS_REMOVE = "/settings/study/remove";
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
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

    @PostMapping(URL_STUDY_SETTINGS_BANNER_ENABLE)
    public String enableStudyBannerImage(@CurrentUser UsersEntity usersEntity, @PathVariable String path) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        studyService.enableStudyBanner(studyEntity);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_SETTINGS_BANNER;
    }

    @PostMapping(URL_STUDY_SETTINGS_BANNER_DISABLE)
    public String unableStudyBannerImage(@CurrentUser UsersEntity usersEntity, @PathVariable String path) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        studyService.disableStudyBanner(studyEntity);
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_SETTINGS_BANNER;
    }

    @GetMapping(URL_STUDY_SETTING_TAGS)
    public String studyTagsUpdateView(@CurrentUser UsersEntity usersEntity, @PathVariable String path, Model model) throws JsonProcessingException {
        StudyEntity studyEntity = studyService.getStudyToUpdateTag(usersEntity, path);

        model.addAttribute("user", usersEntity);
        model.addAttribute("study", studyEntity);

        List<String> tags = studyService.getStudyTags(studyEntity).stream().map(TagEntity::getTitle).collect(Collectors.toList());
        List<String> whitelist = tagRepository.findAll().stream().map(TagEntity::getTitle).collect(Collectors.toList());

        model.addAttribute("tags", tags);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(whitelist));

        return VIEW_STUDY_SETTINGS_TAGS;
    }

    @PostMapping(URL_STUDY_SETTING_TAGS_ADD)
    @ResponseBody
    public ResponseEntity addStudyTags(@CurrentUser UsersEntity usersEntity, @PathVariable String path
            , @RequestBody TagForm tagForm) {
        StudyEntity studyEntity = studyService.getStudyToUpdateTag(usersEntity, path);
        TagEntity tagEntity = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(studyEntity, tagEntity);
        return ResponseEntity.ok().build();
    }

    @PostMapping(URL_STUDY_SETTING_TAGS_REMOVE)
    @ResponseBody
    public ResponseEntity removeStudyTags(@CurrentUser UsersEntity usersEntity, @PathVariable String path
            , @RequestBody TagForm tagForm) {
        StudyEntity studyEntity = studyService.getStudyToUpdateTag(usersEntity, path);
        Optional<TagEntity> optionalTagEntity = tagRepository.findByTitle(tagForm.getTagTitle());
        if (optionalTagEntity.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        TagEntity tagEntity = optionalTagEntity.get();
        studyService.removeTag(studyEntity, tagEntity);
        return ResponseEntity.ok().build();
    }

    @GetMapping(URL_STUDY_SETTING_ZONES)
    public String studyZonesUpdateView(@CurrentUser UsersEntity usersEntity, @PathVariable String path, Model model) throws JsonProcessingException {
        StudyEntity studyEntity = studyService.getStudyToUpdateZone(usersEntity, path);

        model.addAttribute("user", usersEntity);
        model.addAttribute("study", studyEntity);

        List<String> zones = studyService.getStudyZones(studyEntity).stream().map(ZoneEntity::toString).collect(Collectors.toList());
        List<String> allZones = zoneRepository.findAll().stream().map(ZoneEntity::toString).collect(Collectors.toList());

        model.addAttribute("zones", zones);
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return VIEW_STUDY_SETTINGS_ZONES;
    }

    @PostMapping(URL_STUDY_SETTING_ZONES_ADD)
    @ResponseBody
    public ResponseEntity addStudyZone(@CurrentUser UsersEntity usersEntity, @PathVariable String path, @RequestBody ZoneForm zoneForm) throws JsonProcessingException {
        StudyEntity studyEntity = studyService.getStudyToUpdateZone(usersEntity, path);

        Optional<ZoneEntity> optionalZone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (optionalZone.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ZoneEntity zoneEntity = optionalZone.get();
        studyService.addZone(studyEntity, zoneEntity);

        return ResponseEntity.ok().build();
    }

    @PostMapping(URL_STUDY_SETTING_ZONES_REMOVE)
    @ResponseBody
    public ResponseEntity removeStudyZone(@CurrentUser UsersEntity usersEntity, @PathVariable String path, @RequestBody ZoneForm zoneForm) throws JsonProcessingException {
        StudyEntity studyEntity = studyService.getStudyToUpdateZone(usersEntity, path);

        Optional<ZoneEntity> optionalZone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (optionalZone.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ZoneEntity zoneEntity = optionalZone.get();
        studyService.removeZone(studyEntity, zoneEntity);

        return ResponseEntity.ok().build();
    }

    @GetMapping(URL_STUDY_SETTING)
    public String studySettingView(@CurrentUser UsersEntity usersEntity, @PathVariable String path, Model model) {
        StudyEntity studyEntity = studyService.getStudyToUpdate(usersEntity, path);
        model.addAttribute("user", usersEntity);
        model.addAttribute("study", studyEntity);
        return VIEW_STUDY_SETTINGS_STUDY;
    }

    @PostMapping(URL_STUDY_SETTINGS_PUBLISH)
    public String publishStudy(@CurrentUser UsersEntity usersEntity, @PathVariable String path, RedirectAttributes attributes) {
        studyService.publishStudy(usersEntity, path);
        attributes.addFlashAttribute("message", "스터디 공개");
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_SETTING;
    }

    @PostMapping(URL_STUDY_SETTINGS_CLOSE)
    public String closeStudy(@CurrentUser UsersEntity usersEntity, @PathVariable String path, RedirectAttributes attributes) {
        studyService.closeStudy(usersEntity, path);
        attributes.addFlashAttribute("message", "스터디 종료");
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_SETTING;
    }

    @PostMapping(URL_STUDY_RECRUIT_START)
    public String startRecruit(@CurrentUser UsersEntity usersEntity, @PathVariable String path, RedirectAttributes attributes) {
        StudyEntity studyEntity = studyService.getStudyToUpdateStatus(usersEntity, path);

        if (!studyEntity.isRecruitUpdatable()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_SETTING;
        }

        studyService.startStudyRecruit(studyEntity);
        attributes.addFlashAttribute("message", "인원 모집 시작");
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_SETTING;
    }

    @PostMapping(URL_STUDY_RECRUIT_STOP)
    public String stopRecruit(@CurrentUser UsersEntity usersEntity, @PathVariable String path, RedirectAttributes attributes) {
        StudyEntity studyEntity = studyService.getStudyToUpdateStatus(usersEntity, path);

        if (!studyEntity.isRecruitUpdatable()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_SETTING;
        }

        studyService.stopStudyRecruit(studyEntity);
        attributes.addFlashAttribute("message", "인원 모집 종료");
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_SETTING;
    }

    @PostMapping(URL_STUDY_SETTINGS_PATH)
    public String updateStudyPath(@CurrentUser UsersEntity usersEntity, @PathVariable String path, String newPath,
                                  Model model, RedirectAttributes attributes) {
        StudyEntity studyEntity = studyService.getStudyToUpdateStatus(usersEntity, path);

        if (!studyService.isValidPath(newPath)) {
            model.addAttribute("user", usersEntity);
            model.addAttribute("study", studyEntity);
            model.addAttribute("studyPathError", "해당 스터디 경로는 사용할 수 없습니다.");
            return VIEW_STUDY_SETTINGS_STUDY;
        }

        studyService.updateStudyPath(studyEntity, newPath);
        attributes.addFlashAttribute("message", "스터디 URL 수정 완료");
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(newPath) + URL_STUDY_SETTING;
    }

    @PostMapping(URL_STUDY_SETTINGS_TITLE)
    public String updateStudyTitle(@CurrentUser UsersEntity usersEntity, @PathVariable String path, String newTitle,
                                   Model model, RedirectAttributes attributes) {
        StudyEntity studyEntity = studyService.getStudyToUpdateStatus(usersEntity, path);

        if (!studyService.isValidTitle(newTitle)) {
            model.addAttribute("user", usersEntity);
            model.addAttribute("study", studyEntity);
            model.addAttribute("studyTitleError", "스터디 이름을 다시 입력하세요.");
            return VIEW_STUDY_SETTINGS_STUDY;
        }

        studyService.updateStudyTitle(studyEntity, newTitle);
        attributes.addFlashAttribute("message", "스터디 제목 수정 완료");
        return REDIRECT + URL_STUDY_PATH + getEncodedUrl(path) + URL_STUDY_SETTING;
    }

    @PostMapping(URL_STUDY_SETTINGS_REMOVE)
    public String removeStudy(@CurrentUser UsersEntity usersEntity, @PathVariable String path) {
        StudyEntity studyEntity = studyService.getStudyToUpdateStatus(usersEntity, path);
        studyService.removeStudy(studyEntity);
        return URL_REDIRECT_ROOT;
    }

}
