package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestResponseDto create(Long userId, ItemRequestDto dto) {
        log.info("Creating item request for user id={}", userId);
        User requestor = getUserOrThrow(userId);

        ItemRequest request = ItemRequestMapper.toItemRequest(dto, requestor);
        ItemRequest saved = itemRequestRepository.save(request);
        log.debug("Item request created with id={}", saved.getId());

        return ItemRequestMapper.toResponseDto(saved, Collections.emptyList());
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        log.info("Fetching own requests for user id={}", userId);
        getUserOrThrow(userId);

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        return enrichWithItems(requests);
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, Integer from, Integer size) {
        log.info("Fetching all requests for user id={}, from={}, size={}", userId, from, size);
        getUserOrThrow(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequestorIdNotOrderByCreatedDesc(userId, pageable);
        return enrichWithItems(requests);
    }

    @Override
    public ItemRequestResponseDto getById(Long userId, Long requestId) {
        log.info("Fetching request id={} for user id={}", requestId, userId);
        getUserOrThrow(userId);

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found with id: " + requestId));

        List<Item> items = itemRepository.findAllByRequestId(requestId);
        return ItemRequestMapper.toResponseDto(request, items);
    }

    private List<ItemRequestResponseDto> enrichWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> allItems = itemRepository.findAllByRequestIdIn(requestIds);
        Map<Long, List<Item>> itemsByRequest = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> ItemRequestMapper.toResponseDto(
                        request,
                        itemsByRequest.getOrDefault(request.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    }
}