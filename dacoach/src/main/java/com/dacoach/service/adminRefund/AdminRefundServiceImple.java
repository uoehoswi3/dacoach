package com.dacoach.service.adminRefund;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dacoach.kakaopay.KakaoCancelResponse;
import com.dacoach.kakaopay.KakaoPayService;
import com.dacoach.kakaopay.PayDTO;
import com.dacoach.mapper.kakaopay.KakaopayMapper;

@Service
public class AdminRefundServiceImple implements AdminRefundService{

	@Autowired
	private KakaopayMapper kakakaoMapper;
	@Autowired
	private KakaoPayService kakaoPayService;
	@Override
	public List<Map<String, Object>> getRefundList(int startRow,int endRow) {
		try {
			return kakakaoMapper.selectRefundList(startRow,endRow);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	@Transactional(rollbackFor = Exception.class)
@Override
public String approveRefund(int pay_idx) {
    try {
        PayDTO payInfo = kakakaoMapper.paySelect(pay_idx);
        
        if(payInfo == null) return "NOT_FOUND";
        
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("cid", payInfo.getCid());
        parameters.put("tid", payInfo.getTid());
        parameters.put("cancel_amount", payInfo.getTotal());
        parameters.put("cancel_vat_amount", payInfo.getVat());
        parameters.put("cancel_tax_free_amount", payInfo.getTax_free());
        parameters.put("payload", payInfo.getPayload());
        
        KakaoCancelResponse response = kakaoPayService.cancelResponse(parameters);
        
        if(response != null && "CANCEL_PAYMENT".equals(response.getStatus())) {
            kakakaoMapper.cancelOk(response);
            
            Map<String,String> status_map = new HashMap<>();
            status_map.put("status", "환불완료");
            status_map.put("tid", payInfo.getTid());
            kakakaoMapper.upPayStatus(status_map);
            
            return "SUCCESS";
        }
        
        return "FAIL";
        
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("환불 처리 중 오류가 발생했습니다.", e);
    }
}

	@Override
	public int getRefundTotalCnt() {
		return kakakaoMapper.getRefundTotalCnt();
	}

}
