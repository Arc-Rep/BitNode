package bdcc.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.28.0)",
    comments = "Source: grpc_bitnode.proto")
public final class NodeOperationsGrpc {

  private NodeOperationsGrpc() {}

  public static final String SERVICE_NAME = "grpc.NodeOperations";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<bdcc.grpc.NodeInfo,
      bdcc.grpc.NodeInfo> getNotifyNodeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "notifyNode",
      requestType = bdcc.grpc.NodeInfo.class,
      responseType = bdcc.grpc.NodeInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<bdcc.grpc.NodeInfo,
      bdcc.grpc.NodeInfo> getNotifyNodeMethod() {
    io.grpc.MethodDescriptor<bdcc.grpc.NodeInfo, bdcc.grpc.NodeInfo> getNotifyNodeMethod;
    if ((getNotifyNodeMethod = NodeOperationsGrpc.getNotifyNodeMethod) == null) {
      synchronized (NodeOperationsGrpc.class) {
        if ((getNotifyNodeMethod = NodeOperationsGrpc.getNotifyNodeMethod) == null) {
          NodeOperationsGrpc.getNotifyNodeMethod = getNotifyNodeMethod =
              io.grpc.MethodDescriptor.<bdcc.grpc.NodeInfo, bdcc.grpc.NodeInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "notifyNode"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  bdcc.grpc.NodeInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  bdcc.grpc.NodeInfo.getDefaultInstance()))
              .setSchemaDescriptor(new NodeOperationsMethodDescriptorSupplier("notifyNode"))
              .build();
        }
      }
    }
    return getNotifyNodeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<bdcc.grpc.NodeInfo,
      bdcc.grpc.NodeInfo> getFindNodeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "findNode",
      requestType = bdcc.grpc.NodeInfo.class,
      responseType = bdcc.grpc.NodeInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<bdcc.grpc.NodeInfo,
      bdcc.grpc.NodeInfo> getFindNodeMethod() {
    io.grpc.MethodDescriptor<bdcc.grpc.NodeInfo, bdcc.grpc.NodeInfo> getFindNodeMethod;
    if ((getFindNodeMethod = NodeOperationsGrpc.getFindNodeMethod) == null) {
      synchronized (NodeOperationsGrpc.class) {
        if ((getFindNodeMethod = NodeOperationsGrpc.getFindNodeMethod) == null) {
          NodeOperationsGrpc.getFindNodeMethod = getFindNodeMethod =
              io.grpc.MethodDescriptor.<bdcc.grpc.NodeInfo, bdcc.grpc.NodeInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "findNode"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  bdcc.grpc.NodeInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  bdcc.grpc.NodeInfo.getDefaultInstance()))
              .setSchemaDescriptor(new NodeOperationsMethodDescriptorSupplier("findNode"))
              .build();
        }
      }
    }
    return getFindNodeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<bdcc.grpc.NodeInfo,
      bdcc.grpc.NodeInfo> getLookupNodeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "lookupNode",
      requestType = bdcc.grpc.NodeInfo.class,
      responseType = bdcc.grpc.NodeInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<bdcc.grpc.NodeInfo,
      bdcc.grpc.NodeInfo> getLookupNodeMethod() {
    io.grpc.MethodDescriptor<bdcc.grpc.NodeInfo, bdcc.grpc.NodeInfo> getLookupNodeMethod;
    if ((getLookupNodeMethod = NodeOperationsGrpc.getLookupNodeMethod) == null) {
      synchronized (NodeOperationsGrpc.class) {
        if ((getLookupNodeMethod = NodeOperationsGrpc.getLookupNodeMethod) == null) {
          NodeOperationsGrpc.getLookupNodeMethod = getLookupNodeMethod =
              io.grpc.MethodDescriptor.<bdcc.grpc.NodeInfo, bdcc.grpc.NodeInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "lookupNode"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  bdcc.grpc.NodeInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  bdcc.grpc.NodeInfo.getDefaultInstance()))
              .setSchemaDescriptor(new NodeOperationsMethodDescriptorSupplier("lookupNode"))
              .build();
        }
      }
    }
    return getLookupNodeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<bdcc.grpc.NodeInfo,
      bdcc.grpc.NodeInfo> getFindValueMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "findValue",
      requestType = bdcc.grpc.NodeInfo.class,
      responseType = bdcc.grpc.NodeInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<bdcc.grpc.NodeInfo,
      bdcc.grpc.NodeInfo> getFindValueMethod() {
    io.grpc.MethodDescriptor<bdcc.grpc.NodeInfo, bdcc.grpc.NodeInfo> getFindValueMethod;
    if ((getFindValueMethod = NodeOperationsGrpc.getFindValueMethod) == null) {
      synchronized (NodeOperationsGrpc.class) {
        if ((getFindValueMethod = NodeOperationsGrpc.getFindValueMethod) == null) {
          NodeOperationsGrpc.getFindValueMethod = getFindValueMethod =
              io.grpc.MethodDescriptor.<bdcc.grpc.NodeInfo, bdcc.grpc.NodeInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "findValue"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  bdcc.grpc.NodeInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  bdcc.grpc.NodeInfo.getDefaultInstance()))
              .setSchemaDescriptor(new NodeOperationsMethodDescriptorSupplier("findValue"))
              .build();
        }
      }
    }
    return getFindValueMethod;
  }

  private static volatile io.grpc.MethodDescriptor<bdcc.grpc.TransactionInfo,
      bdcc.grpc.NodeResponse> getMakeTransactionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "makeTransaction",
      requestType = bdcc.grpc.TransactionInfo.class,
      responseType = bdcc.grpc.NodeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<bdcc.grpc.TransactionInfo,
      bdcc.grpc.NodeResponse> getMakeTransactionMethod() {
    io.grpc.MethodDescriptor<bdcc.grpc.TransactionInfo, bdcc.grpc.NodeResponse> getMakeTransactionMethod;
    if ((getMakeTransactionMethod = NodeOperationsGrpc.getMakeTransactionMethod) == null) {
      synchronized (NodeOperationsGrpc.class) {
        if ((getMakeTransactionMethod = NodeOperationsGrpc.getMakeTransactionMethod) == null) {
          NodeOperationsGrpc.getMakeTransactionMethod = getMakeTransactionMethod =
              io.grpc.MethodDescriptor.<bdcc.grpc.TransactionInfo, bdcc.grpc.NodeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "makeTransaction"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  bdcc.grpc.TransactionInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  bdcc.grpc.NodeResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NodeOperationsMethodDescriptorSupplier("makeTransaction"))
              .build();
        }
      }
    }
    return getMakeTransactionMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static NodeOperationsStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NodeOperationsStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NodeOperationsStub>() {
        @java.lang.Override
        public NodeOperationsStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NodeOperationsStub(channel, callOptions);
        }
      };
    return NodeOperationsStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static NodeOperationsBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NodeOperationsBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NodeOperationsBlockingStub>() {
        @java.lang.Override
        public NodeOperationsBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NodeOperationsBlockingStub(channel, callOptions);
        }
      };
    return NodeOperationsBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static NodeOperationsFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NodeOperationsFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NodeOperationsFutureStub>() {
        @java.lang.Override
        public NodeOperationsFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NodeOperationsFutureStub(channel, callOptions);
        }
      };
    return NodeOperationsFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class NodeOperationsImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Notify node
     * </pre>
     */
    public void notifyNode(bdcc.grpc.NodeInfo request,
        io.grpc.stub.StreamObserver<bdcc.grpc.NodeInfo> responseObserver) {
      asyncUnimplementedUnaryCall(getNotifyNodeMethod(), responseObserver);
    }

    /**
     * <pre>
     * Find Node
     * </pre>
     */
    public void findNode(bdcc.grpc.NodeInfo request,
        io.grpc.stub.StreamObserver<bdcc.grpc.NodeInfo> responseObserver) {
      asyncUnimplementedUnaryCall(getFindNodeMethod(), responseObserver);
    }

    /**
     * <pre>
     * Initial node lookup
     * </pre>
     */
    public void lookupNode(bdcc.grpc.NodeInfo request,
        io.grpc.stub.StreamObserver<bdcc.grpc.NodeInfo> responseObserver) {
      asyncUnimplementedUnaryCall(getLookupNodeMethod(), responseObserver);
    }

    /**
     * <pre>
     * Find if node has value
     * </pre>
     */
    public void findValue(bdcc.grpc.NodeInfo request,
        io.grpc.stub.StreamObserver<bdcc.grpc.NodeInfo> responseObserver) {
      asyncUnimplementedUnaryCall(getFindValueMethod(), responseObserver);
    }

    /**
     */
    public void makeTransaction(bdcc.grpc.TransactionInfo request,
        io.grpc.stub.StreamObserver<bdcc.grpc.NodeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getMakeTransactionMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getNotifyNodeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                bdcc.grpc.NodeInfo,
                bdcc.grpc.NodeInfo>(
                  this, METHODID_NOTIFY_NODE)))
          .addMethod(
            getFindNodeMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                bdcc.grpc.NodeInfo,
                bdcc.grpc.NodeInfo>(
                  this, METHODID_FIND_NODE)))
          .addMethod(
            getLookupNodeMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                bdcc.grpc.NodeInfo,
                bdcc.grpc.NodeInfo>(
                  this, METHODID_LOOKUP_NODE)))
          .addMethod(
            getFindValueMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                bdcc.grpc.NodeInfo,
                bdcc.grpc.NodeInfo>(
                  this, METHODID_FIND_VALUE)))
          .addMethod(
            getMakeTransactionMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                bdcc.grpc.TransactionInfo,
                bdcc.grpc.NodeResponse>(
                  this, METHODID_MAKE_TRANSACTION)))
          .build();
    }
  }

  /**
   */
  public static final class NodeOperationsStub extends io.grpc.stub.AbstractAsyncStub<NodeOperationsStub> {
    private NodeOperationsStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NodeOperationsStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NodeOperationsStub(channel, callOptions);
    }

    /**
     * <pre>
     * Notify node
     * </pre>
     */
    public void notifyNode(bdcc.grpc.NodeInfo request,
        io.grpc.stub.StreamObserver<bdcc.grpc.NodeInfo> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getNotifyNodeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Find Node
     * </pre>
     */
    public void findNode(bdcc.grpc.NodeInfo request,
        io.grpc.stub.StreamObserver<bdcc.grpc.NodeInfo> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getFindNodeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Initial node lookup
     * </pre>
     */
    public void lookupNode(bdcc.grpc.NodeInfo request,
        io.grpc.stub.StreamObserver<bdcc.grpc.NodeInfo> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getLookupNodeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Find if node has value
     * </pre>
     */
    public void findValue(bdcc.grpc.NodeInfo request,
        io.grpc.stub.StreamObserver<bdcc.grpc.NodeInfo> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getFindValueMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void makeTransaction(bdcc.grpc.TransactionInfo request,
        io.grpc.stub.StreamObserver<bdcc.grpc.NodeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getMakeTransactionMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class NodeOperationsBlockingStub extends io.grpc.stub.AbstractBlockingStub<NodeOperationsBlockingStub> {
    private NodeOperationsBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NodeOperationsBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NodeOperationsBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Notify node
     * </pre>
     */
    public bdcc.grpc.NodeInfo notifyNode(bdcc.grpc.NodeInfo request) {
      return blockingUnaryCall(
          getChannel(), getNotifyNodeMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Find Node
     * </pre>
     */
    public java.util.Iterator<bdcc.grpc.NodeInfo> findNode(
        bdcc.grpc.NodeInfo request) {
      return blockingServerStreamingCall(
          getChannel(), getFindNodeMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Initial node lookup
     * </pre>
     */
    public java.util.Iterator<bdcc.grpc.NodeInfo> lookupNode(
        bdcc.grpc.NodeInfo request) {
      return blockingServerStreamingCall(
          getChannel(), getLookupNodeMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Find if node has value
     * </pre>
     */
    public java.util.Iterator<bdcc.grpc.NodeInfo> findValue(
        bdcc.grpc.NodeInfo request) {
      return blockingServerStreamingCall(
          getChannel(), getFindValueMethod(), getCallOptions(), request);
    }

    /**
     */
    public bdcc.grpc.NodeResponse makeTransaction(bdcc.grpc.TransactionInfo request) {
      return blockingUnaryCall(
          getChannel(), getMakeTransactionMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class NodeOperationsFutureStub extends io.grpc.stub.AbstractFutureStub<NodeOperationsFutureStub> {
    private NodeOperationsFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NodeOperationsFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NodeOperationsFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Notify node
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<bdcc.grpc.NodeInfo> notifyNode(
        bdcc.grpc.NodeInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getNotifyNodeMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<bdcc.grpc.NodeResponse> makeTransaction(
        bdcc.grpc.TransactionInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getMakeTransactionMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_NOTIFY_NODE = 0;
  private static final int METHODID_FIND_NODE = 1;
  private static final int METHODID_LOOKUP_NODE = 2;
  private static final int METHODID_FIND_VALUE = 3;
  private static final int METHODID_MAKE_TRANSACTION = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final NodeOperationsImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(NodeOperationsImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_NOTIFY_NODE:
          serviceImpl.notifyNode((bdcc.grpc.NodeInfo) request,
              (io.grpc.stub.StreamObserver<bdcc.grpc.NodeInfo>) responseObserver);
          break;
        case METHODID_FIND_NODE:
          serviceImpl.findNode((bdcc.grpc.NodeInfo) request,
              (io.grpc.stub.StreamObserver<bdcc.grpc.NodeInfo>) responseObserver);
          break;
        case METHODID_LOOKUP_NODE:
          serviceImpl.lookupNode((bdcc.grpc.NodeInfo) request,
              (io.grpc.stub.StreamObserver<bdcc.grpc.NodeInfo>) responseObserver);
          break;
        case METHODID_FIND_VALUE:
          serviceImpl.findValue((bdcc.grpc.NodeInfo) request,
              (io.grpc.stub.StreamObserver<bdcc.grpc.NodeInfo>) responseObserver);
          break;
        case METHODID_MAKE_TRANSACTION:
          serviceImpl.makeTransaction((bdcc.grpc.TransactionInfo) request,
              (io.grpc.stub.StreamObserver<bdcc.grpc.NodeResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class NodeOperationsBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    NodeOperationsBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return bdcc.grpc.NodeOperationsProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("NodeOperations");
    }
  }

  private static final class NodeOperationsFileDescriptorSupplier
      extends NodeOperationsBaseDescriptorSupplier {
    NodeOperationsFileDescriptorSupplier() {}
  }

  private static final class NodeOperationsMethodDescriptorSupplier
      extends NodeOperationsBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    NodeOperationsMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (NodeOperationsGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new NodeOperationsFileDescriptorSupplier())
              .addMethod(getNotifyNodeMethod())
              .addMethod(getFindNodeMethod())
              .addMethod(getLookupNodeMethod())
              .addMethod(getFindValueMethod())
              .addMethod(getMakeTransactionMethod())
              .build();
        }
      }
    }
    return result;
  }
}
