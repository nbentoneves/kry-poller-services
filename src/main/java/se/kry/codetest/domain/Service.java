package se.kry.codetest.domain;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

public final class Service {

    public enum Status {
        OK, FAIL, UNKNOWN
    }

    @SerializedName("name")
    private final String url;

    private final LocalDateTime addedDttm;

    private final Status status;

    private Service(Builder builder) {
        this.url = builder.url;
        this.addedDttm = builder.addedDttm;
        this.status = builder.status;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getAddedDttm() {
        return addedDttm;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Service service = (Service) o;
        return Objects.equals(url, service.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Service.class.getSimpleName() + "[", "]")
                .add("url='" + url + "'")
                .add("addedDttm=" + addedDttm)
                .add("status=" + status)
                .toString();
    }

    public static class Builder {

        private String url;
        private LocalDateTime addedDttm = LocalDateTime.now();
        private Status status = Status.UNKNOWN;

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withAddedDttm(LocalDateTime date) {
            this.addedDttm = date;
            return this;
        }

        public Builder withStatus(Status status) {
            this.status = status;
            return this;
        }

        public Service build() {
            return new Service(this);
        }

    }
}
