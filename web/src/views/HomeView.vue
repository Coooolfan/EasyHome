<script setup lang="ts">
import { defineComponent } from "vue";
import { ref, onUnmounted } from "vue";
import { marked } from "marked";
import { markedHighlight } from "marked-highlight";
import hljs from "highlight.js";
// 注意引入样式，你可以前往 node_module 下查看更多的样式主题
import "highlight.js/styles/base16/darcula.css";
interface Message {
  role: "user" | "assistant";
  content: string;
}

interface StreamResponse {
  content: string;
  role: string;
  finished: boolean;
  aggregationMessage: string;
}

defineComponent({
  name: "HomeView",
});

const userInput = ref("");
const messages = ref<Message[]>([]);
const isLoading = ref(false);
const isTyping = ref(false);
const withHistory = ref(false);

let controller: AbortController | null = null;

const resetAllStates = () => {
  isLoading.value = false;
  isTyping.value = false;
  controller = null;
};

const sendMessage = async (): Promise<void> => {
  if (!userInput.value.trim() || isLoading.value) return;

  // 添加用户消息
  messages.value.push({ role: "user", content: userInput.value });

  // 准备发送请求
  isLoading.value = true;
  isTyping.value = true;

  // 创建新的AbortController用于取消请求
  controller = new AbortController();

  try {
    let prompt = "";
    // 如果开启了历史记录，则添加历史聊天记录
    if (withHistory.value && messages.value.length > 1) {
      // 构建历史消息字符串，排除最后一条用户消息（因为已经在prompt中）
      const historyMessages = messages.value.slice(0, -1);
      prompt +=
        "<ChatHistory>" +
        historyMessages.map((msg) => `${msg.role}: ${msg.content}`).join("\n") +
        "</ChatHistory>";
    }

    prompt += userInput.value;

    let url = `/api/chat/stream?prompt=${encodeURIComponent(prompt)}`;

    // 调用API的流式接口
    const response = await fetch(url, {
      signal: controller.signal,
      headers: {
        Accept: "text/event-stream",
      },
    });

    // 检查响应状态
    if (!response.ok) {
      throw new Error("网络请求失败");
    }

    // 添加空的助手回复
    messages.value.push({ role: "assistant", content: "" });

    // 清空用户输入
    userInput.value = "";

    // 使用原生的流式API处理SSE格式
    const reader = response.body?.getReader();
    if (!reader) throw new Error("无法读取响应流");

    // 读取流数据
    const decoder = new TextDecoder();

    try {
      while (true) {
        const { done, value } = await reader.read();

        if (done) {
          resetAllStates();
          break;
        }

        if (value) {
          // 解码接收到的数据
          const chunk = decoder.decode(value, { stream: true });

          // 处理SSE格式的数据，移除"data:"前缀并解析JSON
          const lines = chunk.split("\n");
          for (const line of lines) {
            if (line.startsWith("data:")) {
              const jsonContent = line.substring(5).trim();
              if (jsonContent) {
                try {
                  const parsedData: StreamResponse = JSON.parse(jsonContent);

                  // 更新最新的AI消息内容为聚合消息
                  messages.value[messages.value.length - 1].content =
                    parsedData.aggregationMessage;

                  // 如果标记为完成，重置状态
                  if (parsedData.finished) {
                    resetAllStates();
                    break;
                  }
                } catch (e) {
                  console.error("JSON解析错误:", e);
                }
              }
            }
          }
        }
      }

      // 处理流结束后可能的最后一个块
      const finalChunk = decoder.decode();
      if (finalChunk) {
        const lines = finalChunk.split("\n");
        for (const line of lines) {
          if (line.startsWith("data:")) {
            const jsonContent = line.substring(5).trim();
            if (jsonContent) {
              try {
                const parsedData: StreamResponse = JSON.parse(jsonContent);
                messages.value[messages.value.length - 1].content =
                  parsedData.aggregationMessage;
              } catch (e) {
                // 忽略解析错误
              }
            }
          }
        }
      }
    } finally {
      resetAllStates();
    }
  } catch (error: any) {
    if (error.name !== "AbortError") {
      console.error("流式请求出错:", error);
      messages.value.push({
        role: "assistant",
        content: "抱歉，发生了错误，请重试。",
      });
    }
    resetAllStates();
  }
};

const cancelRequest = () => {
  if (controller) {
    controller.abort();
    resetAllStates();
  }
};

// 表单提交处理
const handleSubmit = (e: Event) => {
  e.preventDefault(); // 阻止表单默认提交行为
  sendMessage();
};

// 组件卸载时取消可能存在的未完成请求
onUnmounted(() => {
  cancelRequest();
});

marked.use(
  markedHighlight({
    langPrefix: "hljs language-",
    highlight(code, lang) {
      const language = hljs.getLanguage(lang) ? lang : "shell";
      return hljs.highlight(code, { language }).value;
    },
  })
);

const renderedContent = (message: Message) => {
  return marked.parse(message.content);
};
</script>

<template>
  <main class="py-8 bg-gray-50 h-full">
    <div class="max-w-[80%] mx-auto px-4 sm:px-6 lg:px-8">
      <!-- 消息区域 -->
      <div
        v-show="messages.length > 0"
        class="bg-white shadow-sm rounded-xl mb-6 p-8 h-full overflow-y-auto"
      >
        <div
          v-if="messages.length === 0"
          class="flex items-center justify-center h-full"
        >
          <div class="text-center text-gray-400">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-16 w-16 mx-auto mb-4 text-gray-300"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
              />
            </svg>
            <p class="text-xl">开始对话吧...</p>
          </div>
        </div>
        <div class="space-y-6">
          <div
            v-for="(message, index) in messages"
            :key="index"
            class="flex"
            :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
          >
            <div
              class="rounded-2xl px-6 py-4 shadow-sm"
              :class="
                message.role === 'user'
                  ? 'bg-blue-100 text-white'
                  : 'bg-gray-100 text-gray-800'
              "
            >
              <div class="prose" v-html="renderedContent(message)"></div>
            </div>
          </div>
          <div v-if="isTyping" class="flex justify-start">
            <div
              class="bg-gray-100 rounded-2xl px-6 py-4 shadow-sm text-gray-500"
            >
              <span class="dots-loading">AI正在思考</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <form @submit="handleSubmit" class="bg-white shadow-sm rounded-xl p-6">
        <div class="flex items-center">
          <input
            v-model="userInput"
            class="flex-1 border border-gray-200 rounded-lg px-5 py-4 mr-3 text-base focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="输入您的问题..."
            :disabled="isLoading"
          />
          <div class="flex items-center mx-3">
            <input
              type="checkbox"
              id="withHistoryToggle"
              v-model="withHistory"
              class="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
            />
            <label for="withHistoryToggle" class="ml-2 text-sm text-gray-700"
              >携带历史记录</label
            >
          </div>
          <button
            type="submit"
            :disabled="isLoading || !userInput.trim()"
            class="bg-blue-600 hover:bg-blue-700 text-white rounded-lg px-8 py-4 font-medium text-base transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            发送
          </button>
          <button
            v-if="isLoading"
            type="button"
            @click="cancelRequest"
            class="ml-3 bg-gray-500 hover:bg-gray-600 text-white rounded-lg px-6 py-4 transition-colors duration-200"
          >
            取消
          </button>
        </div>
      </form>
    </div>
  </main>
</template>

<style scoped>
.dots-loading:after {
  content: " .";
  animation: dots 1.5s steps(5, end) infinite;
}

@keyframes dots {
  0%,
  20% {
    content: " .";
  }
  40% {
    content: " ..";
  }
  60% {
    content: " ...";
  }
  80%,
  100% {
    content: "";
  }
}
</style>
