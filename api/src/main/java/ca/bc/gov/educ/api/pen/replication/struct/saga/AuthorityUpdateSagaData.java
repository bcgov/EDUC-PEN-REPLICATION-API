package ca.bc.gov.educ.api.pen.replication.struct.saga;

import ca.bc.gov.educ.api.pen.replication.struct.IndependentAuthority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorityUpdateSagaData implements Serializable {

    private static final long serialVersionUID = 6922881826450048299L;

    IndependentAuthority independentAuthority;
}
