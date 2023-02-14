package ca.bc.gov.educ.api.pen.replication.mappers;

import ca.bc.gov.educ.api.pen.replication.model.DistrictMasterEntity;
import ca.bc.gov.educ.api.pen.replication.rest.RestUtils;
import ca.bc.gov.educ.api.pen.replication.struct.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DistrictMapperHelper {

  private static final String MAILING_ADDRESS_TYPE = "MAILING";
  private static final String PHYSICAL_ADDRESS_TYPE = "PHYSICAL";
  private RestUtils restUtils;

  public DistrictMapperHelper(final RestUtils restUtils){
    this.restUtils = restUtils;
  }

  public DistrictMasterEntity toDistrictMaster(District d) {
    Map<String,DistrictRegionCode> districtRegionCodeMap = restUtils.getDistrictRegionCodes();
    final var districtMasterEntity = new DistrictMasterEntity();

    districtMasterEntity.setEmail(StringUtils.substring(d.getEmail(), 0, 100));
    districtMasterEntity.setName(StringUtils.substring(d.getDisplayName(), 0, 40));
    districtMasterEntity.setShortName(StringUtils.substring(d.getDisplayName(), 0, 15));
    districtMasterEntity.setWebAddress(StringUtils.substring(d.getWebsite(),0,100));
    districtMasterEntity.setDistrictAreaCode(districtRegionCodeMap.get(d.getDistrictRegionCode()).getLegacyCode());

    if(StringUtils.isNotEmpty(d.getPhoneNumber())) {
      districtMasterEntity.setPhoneAreaCode(StringUtils.substring(d.getPhoneNumber(), 0, 3));
      districtMasterEntity.setPhoneNumberNoArea(StringUtils.substring(d.getPhoneNumber(), 3, 10));
    }

    if(StringUtils.isNotEmpty(d.getFaxNumber())) {
      districtMasterEntity.setFaxAreaCode(StringUtils.substring(d.getFaxNumber(), 0, 3));
      districtMasterEntity.setFaxNumberNoArea(StringUtils.substring(d.getFaxNumber(), 3, 10));
    }

    if(d.getDistrictStatusCode().equals("ACTIVE")){
      districtMasterEntity.setDistrictStatusCode("O");
    }else{
      districtMasterEntity.setDistrictStatusCode("C");
    }

    //Addresses
    var mailAddress = getAddressValueIfExists(d, MAILING_ADDRESS_TYPE);
    if(mailAddress.isPresent()) {
      var addy = mailAddress.get();
      districtMasterEntity.setMailAddressLine1(StringUtils.substring(addy.getAddressLine1(),0,50));
      districtMasterEntity.setMailAddressLine2(StringUtils.substring(addy.getAddressLine2(),0,50));
      districtMasterEntity.setMailCity(StringUtils.substring(addy.getCity(),0,30));
      districtMasterEntity.setMailProvinceCode(addy.getProvinceCode());
      districtMasterEntity.setMailCountryCode(addy.getCountryCode());
      districtMasterEntity.setMailPostalCode(StringUtils.upperCase(StringUtils.substring(StringUtils.deleteWhitespace(addy.getPostal()),0,6)));
    } else {
      districtMasterEntity.setMailAddressLine1(null);
      districtMasterEntity.setMailAddressLine2(null);
      districtMasterEntity.setMailCity(null);
      districtMasterEntity.setMailProvinceCode(null);
      districtMasterEntity.setMailCountryCode(null);
      districtMasterEntity.setMailPostalCode(null);
    }

    var physAddress = getAddressValueIfExists(d, PHYSICAL_ADDRESS_TYPE);
    if(physAddress.isPresent()) {
      var addy = physAddress.get();
      districtMasterEntity.setPhysAddressLine1(StringUtils.substring(addy.getAddressLine1(),0,50));
      districtMasterEntity.setPhysAddressLine2(StringUtils.substring(addy.getAddressLine2(),0,50));
      districtMasterEntity.setPhysCity(StringUtils.substring(addy.getCity(),0,30));
      districtMasterEntity.setPhysProvinceCode(addy.getProvinceCode());
      districtMasterEntity.setPhysCountryCode(addy.getCountryCode());
      districtMasterEntity.setPhysPostalCode(StringUtils.upperCase(StringUtils.substring(StringUtils.deleteWhitespace(addy.getPostal()),0,6)));
    } else {
      districtMasterEntity.setPhysAddressLine1(null);
      districtMasterEntity.setPhysAddressLine2(null);
      districtMasterEntity.setPhysCity(null);
      districtMasterEntity.setPhysProvinceCode(null);
      districtMasterEntity.setPhysCountryCode(null);
      districtMasterEntity.setPhysPostalCode(null);
    }

    return districtMasterEntity;
  }

  private Optional<DistrictAddress> getAddressValueIfExists(District district, String addressTypeCode){
    if(district.getAddresses() != null){
      var addresses = district.getAddresses().stream().filter(districtAddress -> districtAddress.getAddressTypeCode().equals(addressTypeCode)).collect(Collectors.toList());
      if(!addresses.isEmpty()){
        return Optional.of(addresses.get(0));
      }
    }
    return Optional.empty();
  }

}
