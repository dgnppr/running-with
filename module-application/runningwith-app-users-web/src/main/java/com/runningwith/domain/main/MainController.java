package com.runningwith.domain.main;

import com.runningwith.domain.event.EnrollmentRepository;
import com.runningwith.domain.study.StudyEntity;
import com.runningwith.domain.study.StudyRepository;
import com.runningwith.domain.users.CurrentUser;
import com.runningwith.domain.users.UsersEntity;
import com.runningwith.domain.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static com.runningwith.infra.utils.WebUtils.URL_ROOT;
import static com.runningwith.infra.utils.WebUtils.VIEW_INDEX;

@Controller
@RequiredArgsConstructor
public class MainController {

    public static final String URL_LOGIN = "/login";
    public static final String PAGE_LOGIN = "login";
    public static final String URL_SEARCH_STUDY = "/search/study";
    public static final String VIEW_SEARCH = "search";
    public static final String VIEW_INDEX_AFTER_LOGIN = "index-after-login";
    private final StudyRepository studyRepository;
    private final UsersRepository usersRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping(URL_ROOT)
    public String index(@CurrentUser UsersEntity usersEntity, Model model) {

        if (usersEntity != null) {
            UsersEntity usersLoaded = usersRepository.findUsersEntityWithTagsAndZonesById(usersEntity.getId());
            putStudyInfoRelatedToUsers(usersEntity, model, usersLoaded);
            return VIEW_INDEX_AFTER_LOGIN;
        }

        model.addAttribute("studyList", studyRepository.findFirst9ByPublishedAndClosedOrderByPublishedDatetimeDesc(true, false));
        return VIEW_INDEX;
    }

    @GetMapping(URL_LOGIN)
    public String login(@CurrentUser UsersEntity usersEntity) {
        if (usersEntity != null) {
            return VIEW_INDEX;
        }
        return PAGE_LOGIN;
    }

    @GetMapping(URL_SEARCH_STUDY)
    public String searchStudy(String keyword, Model model,
                              @PageableDefault(size = 9, sort = "publishedDatetime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<StudyEntity> studyPage = studyRepository.findByKeyword(keyword, pageable);
        model.addAttribute("studyPage", studyPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty",
                pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return VIEW_SEARCH;
    }

    private void putStudyInfoRelatedToUsers(UsersEntity usersEntity, Model model, UsersEntity usersLoaded) {
        model.addAttribute("user", usersLoaded);
        model.addAttribute("enrollmentList", enrollmentRepository.findByUsersEntityAndAcceptedOrderByEnrolledAtDesc(usersLoaded, true));
        model.addAttribute("studyList", studyRepository.findByUsers(
                usersLoaded.getTags(),
                usersLoaded.getZones()));
        model.addAttribute("studyManagerOf",
                studyRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDatetimeDesc(usersEntity, false));
        model.addAttribute("studyMemberOf",
                studyRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDatetimeDesc(usersEntity, false));
    }
}
