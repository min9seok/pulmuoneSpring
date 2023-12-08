package org.pro.pulmuone.controller.order;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.pro.pulmuone.domain.member.MemberDTO;
import org.pro.pulmuone.domain.order.daily.DailyItemInfoDTO;
import org.pro.pulmuone.domain.order.daily.DailyOrderItemDTO;
import org.pro.pulmuone.service.order.BoxOrderServiceImpl;
import org.pro.pulmuone.service.order.DailyOrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/daily/order/*")
@Log4j
public class DailyOrderController {

	@Autowired
	private DailyOrderServiceImpl dailyOrderServiceImpl;

	@Autowired
	private BoxOrderServiceImpl boxOrderServiceImpl;
	

	@GetMapping("step1")
	public String step1(@RequestParam(name = "item") String itemsStr , Model model) {
		log.info("> DailyOrderController.step1 ...");
		ObjectMapper objectMapper = new ObjectMapper();

		// {"item":[{"itemCode":"0071654","dayQty":[1,0,1,0,1]}]}
		// -> [{"itemCode":"0071654","dayQty":[1,0,1,0,1]}]
		itemsStr = itemsStr.substring(8, itemsStr.length());

		try {
			// 장바구니 정보 json -> 객체로 가져오기
			List<DailyOrderItemDTO> items = Arrays.asList(objectMapper.readValue(itemsStr, DailyOrderItemDTO[].class));

			// 상품 정보 가져오기
			List<DailyItemInfoDTO> itemInfos = dailyOrderServiceImpl.selectItems(items);

			// 주문 갯수 담기
			Iterator<DailyOrderItemDTO> ir = items.iterator();
			int index = 0;
			while (ir.hasNext()) {
				DailyOrderItemDTO item = (DailyOrderItemDTO) ir.next();
				itemInfos.get(index).setProducts_cnt(item.getDayQty());
				index++;
			} // while

			// 상품 정보 전달
			model.addAttribute("itemInfos", itemInfos);

		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} // try


		// 2. 사용자 정보 출력
		// 현재 사용자의 인증 정보 가져오기
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String username = "";
		// 사용자 id 가져오기
		if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			username = userDetails.getUsername();
		} // if

		// 전화번호 전달
		MemberDTO member = boxOrderServiceImpl.getMemberInfo(username);
		model.addAttribute("member", member);
		
		

		return "order/daily/step1.tiles";
	}

}
