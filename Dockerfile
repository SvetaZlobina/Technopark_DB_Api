FROM ubuntu:16.04

MAINTAINER Svetlana V. Zlobina

# ����������� ������ �������
RUN apt-get -y update

#
# ��������� postgresql
#
ENV PGVER 9.5
RUN apt-get install -y postgresql-$PGVER

# Run the rest of the commands as the ``postgres`` user created by the ``postgres-$PGVER`` package when it was ``apt-get installed``
USER postgres

# Create a PostgreSQL role named ``docker`` with ``docker`` as the password and
# then create a database `docker` owned by the ``docker`` role.
RUN /etc/init.d/postgresql start &&\
    psql --command "CREATE USER docker WITH SUPERUSER PASSWORD 'docker';" &&\
    createdb -E UTF8 -T template0 -O docker docker &&\
    /etc/init.d/postgresql stop

# Adjust PostgreSQL configuration so that remote connections to the
# database are possible.
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/$PGVER/main/pg_hba.conf

# And add ``listen_addresses`` to ``/etc/postgresql/$PGVER/main/postgresql.conf``
RUN echo "listen_addresses='*'" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "synchronous_commit = off" >> /etc/postgresql/$PGVER/main/postgresql.conf

# Expose the PostgreSQL port
EXPOSE 5432

# Add VOLUMEs to allow backup of config, logs and databases
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

# Back to the root user
USER root

#
# ������ �������
#

# ��������� JDK
RUN apt-get install -y openjdk-8-jdk-headless
RUN apt-get install -y maven

# �������� �������� ��� � Docker-���������
ENV WORK /opt/Technopark_DB_Api
ADD src/main/ $WORK/src/main/

# �������� � ������������� �����
WORKDIR $WORK/src/main
RUN mvn package

# �������� ���� �������
EXPOSE 5000

#
# ��������� PostgreSQL � ������
#
CMD service postgresql start && java -Xmx300M -Xmx300M -jar $WORK/src/main/target/DB_Project-1.0-SNAPSHOT.jar

