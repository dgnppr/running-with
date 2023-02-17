package com.runningwith.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningwith.tag.TagEntity;
import com.runningwith.tag.TagForm;
import com.runningwith.tag.TagRepository;
import com.runningwith.users.form.*;
import com.runningwith.users.validator.NicknameFormValidator;
import com.runningwith.users.validator.PasswordFormValidator;
import com.runningwith.zone.ZoneEntity;
import com.runningwith.zone.ZoneRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.runningwith.users.form.Profile.toProfile;
import static com.runningwith.utils.WebUtils.REDIRECT;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SettingsController {
    public static final String URL_SETTINGS_PROFILE = "/settings/profile";
    public static final String VIEW_SETTINGS_PROFILE = "settings/profile";
    public static final String URL_SETTINGS_PASSWORD = "/settings/password";
    public static final String VIEW_SETTINGS_PASSWORD = "settings/password";
    public static final String URL_SETTINGS_NOTIFICATIONS = "/settings/notifications";
    public static final String VIEW_SETTINGS_NOTIFICATIONS = "settings/notifications";
    public static final String URL_SETTINGS_USERS = "/settings/users";
    public static final String PASSWORD_FORM = "passwordForm";
    public static final String VIEW_SETTINGS_USERS = "settings/users";
    public static final String NOTIFICATIONS_FORM = "notifications";
    public static final String NICKNAME_FORM = "nicknameForm";
    public static final String URL_SETTINGS_TAGS = "/settings/tags";
    public static final String VIEW_SETTINGS_TAGS = "settings/tags";
    public static final String URL_SETTINGS_TAGS_ADD = "/settings/tags/add";
    public static final String URL_SETTINGS_TAGS_REMOVE = "/settings/tags/remove";
    public static final String URL_SETTINGS_ZONES = "/settings/zones";
    public static final String VIEW_SETTINGS_ZONES = "settings/zones";
    public static final String URL_SETTINGS_ZONES_ADD = "/settings/zones/add";
    public static final String URL_SETTINGS_ZONES_REMOVE = "/settings/zones/remove";
    private final ZoneRepository zoneRepository;
    private final TagRepository tagRepository;
    private final UsersService usersService;
    private final NicknameFormValidator nicknameFormValidator;
    private final ObjectMapper objectMapper;

    @InitBinder({PASSWORD_FORM})
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder(NICKNAME_FORM)
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameFormValidator);
    }

    @GetMapping(URL_SETTINGS_PROFILE)
    public String profileUpdateView(@CurrentUser UsersEntity usersEntity, Model model) {

        model.addAttribute("profile", toProfile(usersEntity));
        model.addAttribute("nickname", usersEntity.getNickname());
        model.addAttribute("user", usersEntity);

        return VIEW_SETTINGS_PROFILE;
    }

    @PostMapping(URL_SETTINGS_PROFILE)
    public String updateProfile(@CurrentUser UsersEntity usersEntity, @Validated Profile profile, BindingResult bindingResult,
                                Model model, RedirectAttributes attributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("nickname", usersEntity.getNickname());
            return VIEW_SETTINGS_PROFILE;
        }

        usersService.updateProfile(usersEntity, profile);
        attributes.addFlashAttribute("message", "프로필 수정 완료");
        return REDIRECT + URL_SETTINGS_PROFILE;
    }

    @GetMapping(URL_SETTINGS_PASSWORD)
    public String updatePasswordView(@CurrentUser UsersEntity usersEntity, Model model) {
        model.addAttribute("user", usersEntity);
        model.addAttribute(new PasswordForm());
        return VIEW_SETTINGS_PASSWORD;
    }

    @PostMapping(URL_SETTINGS_PASSWORD)
    public String updatePassword(@CurrentUser UsersEntity usersEntity, @Validated PasswordForm passwordForm, BindingResult bindingResult
            , Model model, RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", usersEntity);
            return VIEW_SETTINGS_PASSWORD;
        }

        usersService.updatePassword(usersEntity, passwordForm);
        attributes.addFlashAttribute("message", "비밀번호 변경 완료");
        return REDIRECT + URL_SETTINGS_PASSWORD;
    }

    @GetMapping(URL_SETTINGS_NOTIFICATIONS)
    public String updateNotificationsView(@CurrentUser UsersEntity usersEntity, Model model) {
        model.addAttribute("user", usersEntity);
        model.addAttribute(new Notifications(usersEntity));
        return VIEW_SETTINGS_NOTIFICATIONS;
    }

    @PostMapping(URL_SETTINGS_NOTIFICATIONS)
    public String updateNotifications(@CurrentUser UsersEntity usersEntity, @Validated Notifications notifications, BindingResult bindingResult,
                                      Model model, RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", usersEntity);
            return VIEW_SETTINGS_NOTIFICATIONS;
        }

        usersService.updateNotifications(usersEntity, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return REDIRECT + URL_SETTINGS_NOTIFICATIONS;
    }

    @GetMapping(URL_SETTINGS_USERS)
    public String updateUsersView(@CurrentUser UsersEntity usersEntity, Model model) {
        model.addAttribute("user", usersEntity);
        model.addAttribute(new NicknameForm(usersEntity.getNickname()));
        return VIEW_SETTINGS_USERS;
    }

    @PostMapping(URL_SETTINGS_USERS)
    public String updateUsersForm(@CurrentUser UsersEntity usersEntity, @Validated NicknameForm nicknameForm, BindingResult bindingResult,
                                  Model model, RedirectAttributes attributes, HttpServletRequest request, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", usersEntity);
            return VIEW_SETTINGS_USERS;
        }

        usersService.updateNickname(usersEntity, nicknameForm.getNickname());
        usersService.login(usersEntity, request, response);
        attributes.addFlashAttribute("message", "닉네임 변경 완료");
        return REDIRECT + URL_SETTINGS_USERS;
    }

    @GetMapping(URL_SETTINGS_TAGS)
    public String updateTagsView(@CurrentUser UsersEntity usersEntity, Model model) throws JsonProcessingException {
        model.addAttribute("user", usersEntity);

        Set<TagEntity> tags = usersService.getTags(usersEntity);
        model.addAttribute("tags", tags.stream().map(TagEntity::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(TagEntity::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return VIEW_SETTINGS_TAGS;
    }

    @PostMapping(URL_SETTINGS_TAGS_ADD)
    @ResponseBody
    public ResponseEntity addTags(@CurrentUser UsersEntity usersEntity, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();

        Optional<TagEntity> optionalTagEntity = tagRepository.findByTitle(title);
        TagEntity tagEntity;
        if (optionalTagEntity.isEmpty()) {
            tagEntity = tagRepository.save(TagEntity.builder().title(title).build());
        } else {
            tagEntity = optionalTagEntity.get();
        }

        usersService.addTag(usersEntity, tagEntity);
        return ResponseEntity.ok().build();
    }

    @PostMapping(URL_SETTINGS_TAGS_REMOVE)
    @ResponseBody
    public ResponseEntity removeTags(@CurrentUser UsersEntity usersEntity, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Optional<TagEntity> optionalTagEntity = tagRepository.findByTitle(title);
        if (optionalTagEntity.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        TagEntity tagEntity = optionalTagEntity.get();

        usersService.removeTag(usersEntity, tagEntity);
        return ResponseEntity.ok().build();
    }

    @GetMapping(URL_SETTINGS_ZONES)
    public String updateZonesView(@CurrentUser UsersEntity usersEntity, Model model) throws JsonProcessingException {
        model.addAttribute("user", usersEntity);

        Set<ZoneEntity> zones = usersService.getZones(usersEntity);
        model.addAttribute("zones", zones.stream().map(ZoneEntity::toString).collect(Collectors.toList()));

        List<String> whitelist = zoneRepository.findAll().stream().map(ZoneEntity::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(whitelist));

        return VIEW_SETTINGS_ZONES;
    }

    @PostMapping(URL_SETTINGS_ZONES_ADD)
    @ResponseBody
    public ResponseEntity addZones(@CurrentUser UsersEntity usersEntity, @RequestBody ZoneForm zoneForm) {
        Optional<ZoneEntity> optionalZone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());

        if (optionalZone.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ZoneEntity zoneEntity = optionalZone.get();

        usersService.addZone(usersEntity, zoneEntity);
        return ResponseEntity.ok().build();
    }

    @PostMapping(URL_SETTINGS_ZONES_REMOVE)
    @ResponseBody
    public ResponseEntity removeZones(@CurrentUser UsersEntity usersEntity, @RequestBody ZoneForm zoneForm) {

        Optional<ZoneEntity> optionalZone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());

        if (optionalZone.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ZoneEntity zoneEntity = optionalZone.get();

        usersService.removeZone(usersEntity, zoneEntity);
        return ResponseEntity.ok().build();
    }

}
