package com.runningwith.index;

import com.runningwith.MockMvcTest;
import com.runningwith.WithUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static com.runningwith.utils.WebUtils.PAGE_INDEX;
import static com.runningwith.utils.WebUtils.URL_ROOT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class IndexControllerTest {

    @Autowired
    MockMvc mockMvc;

    @WithUser("RANDOM_STRING")
    @DisplayName("인덱스 뷰 - 인증 유저")
    @Test
    void index_with_authenticated_user() throws Exception {
        mockMvc.perform(get(URL_ROOT))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("usersEntity"))
                .andExpect(view().name(PAGE_INDEX));
    }

    @DisplayName("인덱스 뷰 - 익명 유저")
    @Test
    void index_with_unauthenticated_user() throws Exception {
        mockMvc.perform(get(URL_ROOT))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("usersEntity"))
                .andExpect(view().name(PAGE_INDEX));
    }


}