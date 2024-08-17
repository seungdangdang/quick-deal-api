package com.quickdeal.product.infrastructure.entity;

import lombok.Getter;

@Getter
public enum CategoryType {
  FASHION("패션의류/잡화"),
  BEAUTY("뷰티"),
  BABY_PRODUCTS("출산/유아동"),
  FOOD("식품"),
  KITCHENWARE("주방용품"),
  HOUSEHOLD_GOODS("생활용품"),
  HOME_INTERIOR("홈인테리어"),
  DIGITAL_APPLIANCES("가전디지털"),
  SPORTS_LEISURE("스포츠/레저"),
  AUTOMOTIVE("자동차용품"),
  BOOKS_MEDIA_DVD("도서/음반/DVD"),
  TOOLS_HOBBIES("완구/취미"),
  STATIONERY_OFFICE("문구/오피스"),
  PET_SUPPLIES("반려동물용품"),
  HEALTH_SUPPLEMENTS("헬스/건강식품"),
  TRAVEL_TICKETS("여행/티켓");

  private final String koreanName;

  CategoryType(String koreanName) {
    this.koreanName = koreanName;
  }
}
