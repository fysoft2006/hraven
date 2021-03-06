/*
Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.twitter.hraven;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Job identifier with individual elements of the jobtracker assigned ID parsed
 * apart.  The jobtracker ID is parsed as: job_[epoch]_[sequence]
 *
 */
public class JobId implements Comparable<JobId> {
  protected static final String JOB_ID_SEP = "_";
  /**
   * The jobtracker start time from the job ID, obtained from parsing the
   * center component of the job ID.
   */
  protected long jobEpoch;
  /**
   * The jobtracker assigned sequence number for the job, obtained from parsing
   * the last component of the job ID.
   */
  protected long jobSequence;

  @JsonCreator
  public JobId(@JsonProperty("jobIdString") String jobId) {
    if (jobId != null) {
      String[] elts = jobId.trim().split(JOB_ID_SEP);
      try {
        this.jobEpoch = Long.parseLong(elts[1]);
        this.jobSequence = Long.parseLong(elts[2]);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid job ID '"+jobId+
            "', must be in the format 'job_[0-9]+_[0-9]+'");
      }
    }
  }

  public JobId(long epoch, long seq) {
    this.jobEpoch = epoch;
    this.jobSequence = seq;
  }

  public JobId(JobId idToCopy) {
    if (idToCopy != null) {
      this.jobEpoch = idToCopy.getJobEpoch();
      this.jobSequence = idToCopy.getJobSequence();
    }
  }

  /**
   * Returns the epoch value from the job ID.  The epoch value is generated by simply
   * parsing the date formatted jobtracker start time as a long value.
   * @return
   */
  public long getJobEpoch() {
    return jobEpoch;
  }

  /**
   * Returns the job sequence number obtained from the final component of the job ID.
   * The counter used to assign the sequence number is reset on every jobtracker
   * restart, so sequence values will overlap within the same cluster.  In order
   * to ensure uniqueness of job IDs, the epoch and sequence values must be
   * combined.
   * @return
   */
  public long getJobSequence() {
    return jobSequence;
  }

  public String getJobIdString() {
    return String.format("job_%d_%04d", this.jobEpoch, this.jobSequence);
  }

  public String toString() {
    return getJobIdString();
  }

  /**
   * Compares two JobId objects on the basis of their
   * jobEpoch (jobtracker start time from the job ID)
   * and
   * jobSequence( jobtracker assigned sequence number for the job,)
   *
   * @param other
   * @return 0 if this jobEpoch and jobSequence are equal to
   * 						other jobEpoch and jobSequence,
   *         1 if this jobEpoch and jobSequence are greater than
   *         				other jobEpoch and jobSequence,
   *         -1 if this jobEpoch and jobSequence less than
   *         				other jobEpoch and jobSequence
   *
   */
  @Override
  public int compareTo(JobId o) {
    if (o == null) {
      // nulls sort last
      return -1;
    }

    return new CompareToBuilder()
        .append(this.jobEpoch, o.getJobEpoch())
        .append(this.jobSequence, o.getJobSequence())
        .toComparison();
  }

  @Override
  public boolean equals(Object other) {
    if (other != null && other instanceof JobId) {
      return compareTo((JobId)other) == 0;
    }
    return false;
  }

  @Override
  public int hashCode(){
      return new HashCodeBuilder()
          .append(this.jobEpoch)
          .append(this.jobSequence)
          .toHashCode();
  }
}
