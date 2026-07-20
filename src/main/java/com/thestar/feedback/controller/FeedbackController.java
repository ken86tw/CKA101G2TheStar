package com.thestar.feedback.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.thestar.feedback.entity.FeedbackVO;
import com.thestar.feedback.service.FeedbackService;
import com.thestar.member.entity.MemberVO;
import com.thestar.member.service.MemberService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

	@Autowired
	private FeedbackService service;

	@Autowired
	private MemberService memberService;

	// 會員回報畫面
	@GetMapping("/report")
	public String showReportForm(Model model, HttpSession session) {

		// 1. 從 session 取得目前登入的會員（這裡的屬性名稱 "loggedInMember" 請換成你專案實際設定的名稱）
		MemberVO member = (MemberVO) session.getAttribute("loggedInMember");

		// 2. 防禦性檢查：如果有登入，就撈出資料帶到前端
		if (member != null) {
			model.addAttribute("userEmail", member.getMemberEmail());
			model.addAttribute("memberId", member.getMemberId());
		} else {
			// 如果沒登入，可以選擇導向登入頁面，或是帶入空值
			model.addAttribute("userEmail", "");
			model.addAttribute("memberId", "");
		}

		return "user/feedback/report";
	}

	// 員工管理、回覆畫面
	@GetMapping("/manage")
	public String showFeedbackReply() {
		return "/admin/feedback/reply";
	}

	// @ResponseBody 才能繼續回傳 JSON
	// 接收問題回報
	@PostMapping("/add")
	@ResponseBody
	public FeedbackVO add(@RequestBody FeedbackVO feedback) {
		// 在這裡加入判斷：如果 memberId 為 null，則設定為 0 (訪客 ID)
		if (feedback.getMemberId() == null) {
			feedback.setMemberId(1);
		}

		return service.createFeedback(feedback);
	}

	// 接收來自前端網頁report的請求」，並呼叫服務層（Service）來完成問題回報的資料庫更新
	@PostMapping("/reply")
	@ResponseBody
	public FeedbackVO reply(@RequestParam Integer ticketId, @RequestParam String replyContent,
			@RequestParam Integer employeeId) {
		return service.replyFeedback(ticketId, replyContent, employeeId);
	}

	// 查詢所有問題回報紀錄
	@GetMapping("/all")
	@ResponseBody
	public List<FeedbackVO> findAll() {
		return service.getAllFeedback();
	}

	// 執行寄信，呼叫會員寄信方法
	@PostMapping("/send")
	@ResponseBody
	public String send(@RequestParam Integer ticketId, @RequestParam String email, @RequestParam String message) {

		// 呼叫 Service 層進行寄信
		boolean isSent = service.sendMailToMember(ticketId, email, message);

		if (isSent) {
			return "success"; // 回傳 JSON 給前端處理
		} else {
			return "failure";
		}
	}

	// 自動填入會員Email
	@GetMapping("/getMemberInfo")
	@ResponseBody
	public String getMemberEmail(@RequestParam Integer memberId) {
		// 呼叫 Service 取得物件
		MemberVO member = memberService.getMemberById(memberId);

		// 呼叫正確的 Getter 方法：getMemberEmail()，明確取出字串欄位並回傳
		return member.getMemberEmail();
	}
}