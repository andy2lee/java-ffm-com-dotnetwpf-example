# java-ffm-com-dotnetwpf-example
Java invoke dotnet WPF with Java FFM and COM interface.
Popout WPF windows and button from java ffm API.

┌──────────────────────────────┐
│      Java Application        │
└──────────────┬───────────────┘
               │
               │ Java Foreign Function & Memory API
               │ (FFM API withJDK 22+)
               ▼
┌──────────────────────────────┐
│       Java FFM Layer         │
│                              │
│  MemorySegment               │
│  Arena                       │
│  Linker                      │
│  MethodHandle                │
│                              │
│  Call Native Function        │
└──────────────┬───────────────┘
               │
               │ COM Interface
               │
               ▼
┌──────────────────────────────┐
│          COM Server          │
│                              │
│  C# COM Component            │
│  (.NET Class Library)        │
│                              │
│  [ComVisible]                │
│  Interface                   │
│  GUID                        │
│                              │
└──────────────┬───────────────┘
               │
               │ COM C# Mapping
               ▼
┌──────────────────────────────┐
│          C# WPF App          │
│                              │
│           Window             │
│          COM Object          │
└──────────────────────────────┘
