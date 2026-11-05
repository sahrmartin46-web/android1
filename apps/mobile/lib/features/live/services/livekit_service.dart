import 'package:livekit_client/livekit_client.dart';

class LiveKitService {
  Room? room;
  LocalVideoTrack? localVideoTrack;
  LocalAudioTrack? localAudioTrack;

  Future<void> connectToRoom({
    required String wsUrl,
    required String token,
  }) async {
    room = Room();
    
    // Connect to LiveKit Room
    await room!.connect(
      wsUrl,
      token,
      roomOptions: const RoomOptions(
        adaptiveStream: true,
        dynacast: true,
      ),
    );

    // Publish Camera and Microphone
    localVideoTrack = await LocalVideoTrack.createCameraTrack(
      const CameraCaptureOptions(
        cameraPosition: CameraPosition.front,
      ),
    );
    await room!.localParticipant?.publishVideoTrack(localVideoTrack!);

    localAudioTrack = await LocalAudioTrack.createAudioTrack();
    await room!.localParticipant?.publishAudioTrack(localAudioTrack!);
  }

  Future<void> toggleCamera(bool enable) async {
    if (localVideoTrack != null) {
      if (enable) {
        await localVideoTrack!.unmute();
      } else {
        await localVideoTrack!.mute();
      }
    }
  }

  Future<void> toggleMicrophone(bool mute) async {
    if (localAudioTrack != null) {
      if (mute) {
        await localAudioTrack!.mute();
      } else {
        await localAudioTrack!.unmute();
      }
    }
  }

  Future<void> switchCamera() async {
    if (localVideoTrack != null) {
      await localVideoTrack!.setCameraPosition(
        localVideoTrack!.currentCameraPosition == CameraPosition.front
            ? CameraPosition.back
            : CameraPosition.front,
      );
    }
  }

  Future<void> disconnect() async {
    await room?.disconnect();
    room = null;
  }
}
