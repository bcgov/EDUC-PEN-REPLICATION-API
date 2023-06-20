package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.AuthorityMasterEntity;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
public class AuthorityMapperHelper {

  private static final String MAILING_ADDRESS_TYPE = "MAILING";

  private static final String INDAUTHREP = "INDAUTHREP";
  private RestUtils restUtils;
  private LocalDateTimeMapper dateTimeMapper = new LocalDateTimeMapper();

  public AuthorityMapperHelper(final RestUtils restUtils){
    this.restUtils = restUtils;
  }

  public AuthorityMasterEntity toAuthorityMaster(IndependentAuthority ia, boolean isCreate) {
    Map<String,ProvinceCode> provinceCodes = restUtils.getProvinceCodes();
    Map<String,CountryCode> countryCodes = restUtils.getCountryCodes();
    final var authorityMasterEntity = new AuthorityMasterEntity();

    if(isCreate){
      authorityMasterEntity.setAuthNumber(ia.getAuthorityNumber());
    }

    if(ia.getAuthorityTypeCode().equals("OFFSHORE")){
      authorityMasterEntity.setAuthorityType("O");
    }else{
      authorityMasterEntity.setAuthorityType("I");
    }

    if(StringUtils.isNotEmpty(ia.getClosedDate()) && dateTimeMapper.map(ia.getClosedDate()).isBefore(LocalDateTime.now())){
      authorityMasterEntity.setStatusCode("C");
    }else{
      authorityMasterEntity.setStatusCode("O");
    }

    if(StringUtils.isNotEmpty(ia.getOpenedDate())) {
      authorityMasterEntity.setOpenedDate(ia.getOpenedDate().substring(0, 10).replace("-", ""));
      authorityMasterEntity.setDateOpened(dateTimeMapper.map(ia.getOpenedDate()));
    }else{
      authorityMasterEntity.setOpenedDate(null);
      authorityMasterEntity.setDateOpened(null);
    }

    if(StringUtils.isNotEmpty(ia.getClosedDate())) {
      authorityMasterEntity.setClosedDate(ia.getClosedDate().substring(0, 10).replace("-", ""));
      authorityMasterEntity.setDateClosed(dateTimeMapper.map(ia.getClosedDate()));
    }else{
      authorityMasterEntity.setClosedDate(null);
      authorityMasterEntity.setDateClosed(null);
    }

    authorityMasterEntity.setName(StringUtils.substring(ia.getDisplayName(), 0, 40));
    authorityMasterEntity.setAuthorityNameLong(StringUtils.substring(ia.getDisplayName(), 0, 75));
    authorityMasterEntity.setFaxNumber(StringUtils.substring(ia.getFaxNumber(), 0, 10));
    authorityMasterEntity.setPhoneNumber(StringUtils.substring(ia.getPhoneNumber(), 0, 10));
    authorityMasterEntity.setEmail(StringUtils.substring(ia.getEmail(), 0, 100));

    //Addresses
    var mailAddress = getAddressValueIfExists(ia, MAILING_ADDRESS_TYPE);
    if(mailAddress.isPresent()) {
      var addy = mailAddress.get();
      authorityMasterEntity.setAddressLine1(StringUtils.substring(addy.getAddressLine1(),0,40));
      authorityMasterEntity.setAddressLine2(StringUtils.substring(addy.getAddressLine2(),0,40));
      authorityMasterEntity.setCity(StringUtils.substring(addy.getCity(),0,30));
      authorityMasterEntity.setProvinceCode(provinceCodes.get(addy.getProvinceCode()).getLegacyCode());
      authorityMasterEntity.setCountryCode(countryCodes.get(addy.getCountryCode()).getLegacyCode());
      authorityMasterEntity.setPostalCode(StringUtils.upperCase(StringUtils.substring(StringUtils.deleteWhitespace(addy.getPostal()),0,6)));
    } else {
      authorityMasterEntity.setAddressLine1(null);
      authorityMasterEntity.setAddressLine2(null);
      authorityMasterEntity.setCity(null);
      authorityMasterEntity.setProvinceCode(null);
      authorityMasterEntity.setCountryCode(null);
      authorityMasterEntity.setPostalCode(null);
    }

    //Contact
    var contact = getAuthorityContactIfExists(ia);
    if(contact.isPresent()) {
      var prince = contact.get();
      authorityMasterEntity.setGivenName(StringUtils.substring(prince.getFirstName(),0,25));
      authorityMasterEntity.setSurname(StringUtils.substring(prince.getLastName(),0,25));
    } else {
      authorityMasterEntity.setGivenName(null);
      authorityMasterEntity.setSurname(null);
    }
    authorityMasterEntity.setMiddleName(null);
    authorityMasterEntity.setTitleCode(null);

    return authorityMasterEntity;
  }

  private Optional<AuthorityContact> getAuthorityContactIfExists(IndependentAuthority authority){
    if(authority.getContacts() != null){
      var principals = authority.getContacts().stream().filter(authorityContact -> authorityContact.getAuthorityContactTypeCode().equals(INDAUTHREP)).toList();
      if(!principals.isEmpty()){
        return Optional.of(principals.get(0));
      }
    }
    return Optional.empty();
  }

  private Optional<AuthorityAddress> getAddressValueIfExists(IndependentAuthority ia, String addressTypeCode){
    if(ia.getAddresses() != null){
      var addresses = ia.getAddresses().stream().filter(schoolAddress -> schoolAddress.getAddressTypeCode().equals(addressTypeCode)).toList();
      if(!addresses.isEmpty()){
        return Optional.of(addresses.get(0));
      }
    }
    return Optional.empty();
  }

}
