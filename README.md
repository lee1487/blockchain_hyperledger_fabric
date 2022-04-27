# 하이퍼레저 설정
```

Ubuntu 18.04 설치
---------------------------------------------------------
하이퍼레저 설치전 기본 설치 구성
---------------------------------------------------------
1. curl 설치
$ sudo apt install curl 
$ curl -V

2. 도커 설치
$ curl -fsSL https://get.docker.com/ | sudo sh
$ sudo usermod -aG docker $USER
$ sudo reboot
$ docker -v

3. docker-compose 설치
$ sudo curl -L "https://github.com/docker/compose/releases/download/1.22.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
$ sudo chmod +x /usr/local/bin/docker-compose
$ docker-compose --version

4. go 설치
$ cd /usr/local
$ sudo wget https://storage.googleapis.com/golang/go1.11.1.linux-amd64.tar.gz
$ sudo tar -C /usr/local -xzf go1.11.1.linux-amd64.tar.gz
$ echo 'export PATH=$PATH:/usr/local/go/bin' | sudo tee -a /etc/profile && \
echo 'export GOPATH=$HOME/go' | tee -a $HOME/.bashrc && \
echo 'export PATH=$PATH:$GOROOT/bin:$GOPATH/bin' | tee -a $HOME/.bashrc && \
mkdir -p $HOME/go/{src,pkg,bin}
$ sudo reboot
$ go version

5. git 설치
$ sudo apt install -y git 
$ git --version

6. python설치
$ sudo apt install -y python
$ python --version

7. node, npm 설치
$ curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.2/install.sh | bash
$ sudo reboot
$ nvm install 8
$ nvm use 8
$ npm install npm@5.6.0 -g

8. java 설치
$ sudo add-apt-repository ppa:openjdk-r/ppa
$ sudo apt update
$ sudo apt install openjdk-8-jdk openjdk-8-jre
$ javac -version
$ java -version

9. gradle 설치
$ sudo apt install gradle
$ gradle -v

10. 하이퍼레저 패브릭 설치 
$ cd $GOPATH/src
$ curl -sSL https://bit.ly/2ysbOFE | bash -s -- 1.4.12 1.4.9 0.4.22(안정화 버전)
  -> 1.4.3 1.4.3 0.4.15로 설치후 테스트 진행시 에러 발생 안정화 버전 찾는데 오래걸림


---------------------------------------------------------
하이퍼레저 패브릭 시작하기
  generate: 네트워크 구성 및 환경 설정 파일을 생성한다.
  up: BYFN 네트워크에 관련된 도커 컨테이너를 생성하고 실행한다.
  down: 도커 컨테이너로 실행됐던 네트워크를 종료하고 관련 파일을 삭제헤 설정을 초기화한다 
  
쉘 스크립트의 동작은 모든 커맨드 명령어의 진행을 자동으로 수행하게 돼 있다. 그러나 자동으로 
실행될 경우 어떤 일이 일어나는지 이해하기 어렵기 때문에 실제로 어떻게 만들어지는지 직접 
커맨드 명령어를 실행하면서 이해해보자.
---------------------------------------------------------
$ cd $GOPATH/src/fabric-samples/
$ cp -r ./first-network ./first-network-copy
$ cd first-network
$ ./byfn.sh generate


---------------------------------------------------------
네트워크 구성
---------------------------------------------------------
1. cryptogen 도구를 사용해 인증서를 생성한다.

$ cd $GOPATH/src/fabric-samples/first-network-copy
$ vi crypto-config.yaml		//구성 파악
$ ../bin/cryptogen generate --config=./crypto-config.yaml
$ ls crypto-config

2. configtxgen 도구를 사용해 오더링 서비스 노드의 제네시스 블록을 생성한다.
$ export FABRIC_CFG_PATH=$PWD
$ ../bin/configtxgen -profile TwoOrgsOrdererGenesis -outputBlock ./channel-artifacts/genesis.block

3. configtxgen 도구를 사용해 채널을 생성한다.
$ export CHANNEL_NAME=mychannel
$ ../bin/configtxgen -profile TwoOrgsChannel -outputCreateChannelTx ./channel-artifacts/channel.tx -channelID $CHANNEL_NAME

4. configtxgen 도구를 사용해 Org1MSP에 대한 앵커 피어 노드 트랜잭션 파일을 생성한다.
$ ../bin/configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/Org1MSPanchors.tx -channelID $CHANNEL_NAME -asOrg Org1MSP

5. configtxgen 도구를 사용해 Org2MSP에 대한 앵커 피어 노드 트랜잭션 파일을 생성한다.
$ ../bin/configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/Org2MSPanchors.tx -channelID $CHANNEL_NAME -asOrg Org2MSP

---------------------------------------------------------
네트워크 실행

쉘 스크립트로 up 옵션을 입력했을 때 실행되는 커맨드 명령어를 단계 별로 직접 
입력해 보고 이때 하이퍼레저 패브릭 BYFN 네트워크에서 어떤 일이 일어나는지 분석해본다.
---------------------------------------------------------
$ cd $GOPATH/src/fabric-samples/first-network
$ ./byfn.sh up
$ docker ps
$ ./byfn.sh down

단계별로 네트워크를 시작했을 때 구성되는 BYFN의 프로세스는 다음과 같다 

1. 5개의 노드(오더링 서비스 노드, 4개의 피어 노드) 컨테이너, CLI 컨테이너가 실행되며 총 6개의 컨테이너가 먼저 실행된다.
$ cd $GOPATH/src/fabric-samples/first-network-copy
$ docker-compose -f docker-compose-cli.yaml up -d
$ docker ps 
2. 네트워크 내부의 CLI 컨테이너에 접속해 생성된 채널 트랜잭션 파일인 channel.tx를 가지고 채널을 생성하고 채널 mychannel을 생성하고 모든 피어 노드(2개 조직, 4개 피어 노드)를 가입시킨다.
$ docker start cli
$ docker exec -it cli bash
# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
# CORE_PEER_ADDRESS=peer0.org1.example.com:7051
# CORE_PEER_LOCALMSPID="Org1MSP"
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
# export CHANNEL_NAME=mychannel
# peer channel create -o orderer.example.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/channel.tx --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
# ls
# peer channel join -b mychannel.block

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp 
# CORE_PEER_ADDRESS=peer1.org1.example.com:8051 
# CORE_PEER_LOCALMSPID="Org1MSP" 
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer1.org1.example.com/tls/ca.crt 
# peer channel join -b mychannel.block

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp 
# CORE_PEER_ADDRESS=peer0.org2.example.com:9051 
# CORE_PEER_LOCALMSPID="Org2MSP" 
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt 
# peer channel join -b mychannel.block

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp 
# CORE_PEER_ADDRESS=peer1.org2.example.com:10051 
# CORE_PEER_LOCALMSPID="Org2MSP" 
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer1.org2.example.com/tls/ca.crt 
# peer channel join -b mychannel.block

3. 두 조직의 peer0를 앵커로 가입시킨다.
# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp \
# CORE_PEER_ADDRESS=peer0.org1.example.com:7051 \
# CORE_PEER_LOCALMSPID="Org1MSP" \
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt \
# peer channel update -o orderer.example.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/Org1MSPanchors.tx --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp \
# CORE_PEER_ADDRESS=peer0.org2.example.com:9051 \
# CORE_PEER_LOCALMSPID="Org2MSP" \
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt \
# peer channel update -o orderer.example.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/Org2MSPanchors.tx --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

4. 피어 노드에 체인코드를 설치하고 확인하는 단계다. 이 단계에서 해당 피어 노드의 체인코드를 담당하는 체인코드 컨테이너 3개가 실행된다.
  - 두 조직의 앵커 피어 노드(peer0.org1, peer0.org2)에 체인코드를 설치한다.
  - peer0.org2에 초깃값 a에 100을, b에 200을 설정한다.
  - peer0.org1에서 a의 값을 조회한다.
  - 트랜잭션 호출(invoke)로 a에서 b로 10만큼 이동한다.
  - 체인코드를 갖고 있지 않은 peer1.org2에 체인코드를 설치한다.
  - peer1.org2에서 값 a를 조회한다. 초깃값인 100에서 전송한 10을 뺀 90이 조회된다.
  
# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp \
# CORE_PEER_ADDRESS=peer0.org1.example.com:7051 \
# CORE_PEER_LOCALMSPID="Org1MSP" \
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt \
# peer chaincode install -n mycc -v 1.0 -p github.com/chaincode/chaincode_example02/go/

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp \
# CORE_PEER_ADDRESS=peer0.org2.example.com:9051 \
# CORE_PEER_LOCALMSPID="Org2MSP" \
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt \
# peer chaincode install -n mycc -v 1.0 -p github.com/chaincode/chaincode_example02/go/

# peer chaincode instantiate -o orderer.example.com:7050 --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C $CHANNEL_NAME -n mycc -v 1.0 -c '{"Args":["init","a", "100", "b","200"]}' -P "OR('Org1MSP.peer','Org2MSP.peer')"

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp \
# CORE_PEER_ADDRESS=peer0.org1.example.com:7051 \
# CORE_PEER_LOCALMSPID="Org1MSP" \
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt \
# peer chaincode query -C $CHANNEL_NAME -n mycc -c '{"Args":["query","a"]}'

# peer chaincode invoke -o orderer.example.com:7050 -- tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C $CHANNEL_NAME -n mycc -v 1.0 -c '{"Args":["invoke","a","b","10"]}'

# CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp 
# CORE_PEER_ADDRESS=peer1.org2.example.com:10051 
# CORE_PEER_LOCALMSPID="Org2MSP" 
# CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/org2.example.com/peers/peer1.org2.example.com/tls/ca.crt 
# peer chaincode install -n mycc -v 1.0 -p github.com/chaincode/chaincode_example02/go/

# peer chaincode query -C $CHANNEL_NAME -n mycc -c '{"Args":["query","a"]}'

# exit
$ docker-compose -f docker-compose-cli.yaml down
$ rm -rf channel-artifacts/*.block channel-artifacts/*.tx crypto-config
$ docker rm $(docker ps -aq) -f
```