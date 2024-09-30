package com.be.portfolio.service;

import com.be.portfolio.domain.PortfolioItemVO;
import com.be.portfolio.domain.PortfolioVO;
import com.be.portfolio.dto.req.PortfolioItemReqDto;
import com.be.portfolio.dto.req.PortfolioReqDto;
import com.be.portfolio.dto.res.PortfolioItemResDto;
import com.be.portfolio.dto.res.PortfolioPortionDto;
import com.be.portfolio.dto.res.PortfolioResDto;
import com.be.stock.service.StockService;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public interface PortfolioService {
    PortfolioResDto getPortfolio(int portfolioId);
    List<PortfolioResDto> getPortfolioList(long memberNum);
    List<PortfolioItemResDto> getPortfolioItems(int portfolioId);
    PortfolioResDto createPortfolio(PortfolioReqDto portfolioReqDto, List<PortfolioItemReqDto> portfolioItems, JSONObject stockPrices);
    PortfolioResDto updatePortfolio(PortfolioVO portfolioVO);

    @Scheduled(cron = "0 0 0 * * ?")
    void updateAllPortfolio();

    PortfolioResDto deletePortfolio(int id);
    PortfolioReqDto calculatePortfolio(PortfolioReqDto portfolioReqDto, JSONObject stockPrices);
    PortfolioPortionDto calculatePortion(List<PortfolioItemResDto> portfolioItems);
}
