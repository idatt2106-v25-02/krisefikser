/**
 * Generated by orval v7.9.0 🍺
 * Do not edit manually.
 * Krisefikser API
 * API for the Krisefikser application
 * OpenAPI spec version: 1.0
 */
import { useMutation, useQuery } from '@tanstack/vue-query'
import type {
  DataTag,
  MutationFunction,
  QueryClient,
  QueryFunction,
  QueryKey,
  UseMutationOptions,
  UseMutationReturnType,
  UseQueryOptions,
  UseQueryReturnType,
} from '@tanstack/vue-query'

import { unref } from 'vue'
import type { MaybeRef } from 'vue'

import type { GetNotificationsParams, PageNotificationResponse } from '.././model'

import { customInstance } from '../../axios'
import type { ErrorType } from '../../axios'

type SecondParameter<T extends (...args: never) => unknown> = Parameters<T>[1]

/**
 * Marks all notifications for the authenticated user as read
 * @summary Mark all notifications as read
 */
export const readAll = (options?: SecondParameter<typeof customInstance>) => {
  return customInstance<void>(
    { url: `http://localhost:8080/api/notifications/readAll`, method: 'PUT' },
    options,
  )
}

export const getReadAllMutationOptions = <
  TError = ErrorType<unknown>,
  TContext = unknown,
>(options?: {
  mutation?: UseMutationOptions<Awaited<ReturnType<typeof readAll>>, TError, void, TContext>
  request?: SecondParameter<typeof customInstance>
}): UseMutationOptions<Awaited<ReturnType<typeof readAll>>, TError, void, TContext> => {
  const mutationKey = ['readAll']
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined }

  const mutationFn: MutationFunction<Awaited<ReturnType<typeof readAll>>, void> = () => {
    return readAll(requestOptions)
  }

  return { mutationFn, ...mutationOptions }
}

export type ReadAllMutationResult = NonNullable<Awaited<ReturnType<typeof readAll>>>

export type ReadAllMutationError = ErrorType<unknown>

/**
 * @summary Mark all notifications as read
 */
export const useReadAll = <TError = ErrorType<unknown>, TContext = unknown>(
  options?: {
    mutation?: UseMutationOptions<Awaited<ReturnType<typeof readAll>>, TError, void, TContext>
    request?: SecondParameter<typeof customInstance>
  },
  queryClient?: QueryClient,
): UseMutationReturnType<Awaited<ReturnType<typeof readAll>>, TError, void, TContext> => {
  const mutationOptions = getReadAllMutationOptions(options)

  return useMutation(mutationOptions, queryClient)
}
/**
 * Marks a specific notification as read by its ID
 * @summary Mark notification as read
 */
export const readNotification = (
  id: MaybeRef<string>,
  options?: SecondParameter<typeof customInstance>,
) => {
  id = unref(id)

  return customInstance<void>(
    { url: `http://localhost:8080/api/notifications/read/${id}`, method: 'PUT' },
    options,
  )
}

export const getReadNotificationMutationOptions = <
  TError = ErrorType<void>,
  TContext = unknown,
>(options?: {
  mutation?: UseMutationOptions<
    Awaited<ReturnType<typeof readNotification>>,
    TError,
    { id: string },
    TContext
  >
  request?: SecondParameter<typeof customInstance>
}): UseMutationOptions<
  Awaited<ReturnType<typeof readNotification>>,
  TError,
  { id: string },
  TContext
> => {
  const mutationKey = ['readNotification']
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined }

  const mutationFn: MutationFunction<
    Awaited<ReturnType<typeof readNotification>>,
    { id: string }
  > = (props) => {
    const { id } = props ?? {}

    return readNotification(id, requestOptions)
  }

  return { mutationFn, ...mutationOptions }
}

export type ReadNotificationMutationResult = NonNullable<
  Awaited<ReturnType<typeof readNotification>>
>

export type ReadNotificationMutationError = ErrorType<void>

/**
 * @summary Mark notification as read
 */
export const useReadNotification = <TError = ErrorType<void>, TContext = unknown>(
  options?: {
    mutation?: UseMutationOptions<
      Awaited<ReturnType<typeof readNotification>>,
      TError,
      { id: string },
      TContext
    >
    request?: SecondParameter<typeof customInstance>
  },
  queryClient?: QueryClient,
): UseMutationReturnType<
  Awaited<ReturnType<typeof readNotification>>,
  TError,
  { id: string },
  TContext
> => {
  const mutationOptions = getReadNotificationMutationOptions(options)

  return useMutation(mutationOptions, queryClient)
}
/**
 * Retrieves a page of the users notifications
 * @summary Get all notifications for authenticated user
 */
export const getNotifications = (
  params: MaybeRef<GetNotificationsParams>,
  options?: SecondParameter<typeof customInstance>,
  signal?: AbortSignal,
) => {
  params = unref(params)

  return customInstance<PageNotificationResponse>(
    {
      url: `http://localhost:8080/api/notifications`,
      method: 'GET',
      params: unref(params),
      signal,
    },
    options,
  )
}

export const getGetNotificationsQueryKey = (params: MaybeRef<GetNotificationsParams>) => {
  return ['http:', 'localhost:8080', 'api', 'notifications', ...(params ? [params] : [])] as const
}

export const getGetNotificationsQueryOptions = <
  TData = Awaited<ReturnType<typeof getNotifications>>,
  TError = ErrorType<unknown>,
>(
  params: MaybeRef<GetNotificationsParams>,
  options?: {
    query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getNotifications>>, TError, TData>>
    request?: SecondParameter<typeof customInstance>
  },
) => {
  const { query: queryOptions, request: requestOptions } = options ?? {}

  const queryKey = getGetNotificationsQueryKey(params)

  const queryFn: QueryFunction<Awaited<ReturnType<typeof getNotifications>>> = ({ signal }) =>
    getNotifications(params, requestOptions, signal)

  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<
    Awaited<ReturnType<typeof getNotifications>>,
    TError,
    TData
  >
}

export type GetNotificationsQueryResult = NonNullable<Awaited<ReturnType<typeof getNotifications>>>
export type GetNotificationsQueryError = ErrorType<unknown>

/**
 * @summary Get all notifications for authenticated user
 */

export function useGetNotifications<
  TData = Awaited<ReturnType<typeof getNotifications>>,
  TError = ErrorType<unknown>,
>(
  params: MaybeRef<GetNotificationsParams>,
  options?: {
    query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getNotifications>>, TError, TData>>
    request?: SecondParameter<typeof customInstance>
  },
  queryClient?: QueryClient,
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getGetNotificationsQueryOptions(params, options)

  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & {
    queryKey: DataTag<QueryKey, TData, TError>
  }

  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>

  return query
}

/**
 * Returns the number of unread notifications for the authenticated user
 * @summary Get count of unread notifications
 */
export const getUnreadCount = (
  options?: SecondParameter<typeof customInstance>,
  signal?: AbortSignal,
) => {
  return customInstance<number>(
    { url: `http://localhost:8080/api/notifications/unread`, method: 'GET', signal },
    options,
  )
}

export const getGetUnreadCountQueryKey = () => {
  return ['http:', 'localhost:8080', 'api', 'notifications', 'unread'] as const
}

export const getGetUnreadCountQueryOptions = <
  TData = Awaited<ReturnType<typeof getUnreadCount>>,
  TError = ErrorType<unknown>,
>(options?: {
  query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getUnreadCount>>, TError, TData>>
  request?: SecondParameter<typeof customInstance>
}) => {
  const { query: queryOptions, request: requestOptions } = options ?? {}

  const queryKey = getGetUnreadCountQueryKey()

  const queryFn: QueryFunction<Awaited<ReturnType<typeof getUnreadCount>>> = ({ signal }) =>
    getUnreadCount(requestOptions, signal)

  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<
    Awaited<ReturnType<typeof getUnreadCount>>,
    TError,
    TData
  >
}

export type GetUnreadCountQueryResult = NonNullable<Awaited<ReturnType<typeof getUnreadCount>>>
export type GetUnreadCountQueryError = ErrorType<unknown>

/**
 * @summary Get count of unread notifications
 */

export function useGetUnreadCount<
  TData = Awaited<ReturnType<typeof getUnreadCount>>,
  TError = ErrorType<unknown>,
>(
  options?: {
    query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof getUnreadCount>>, TError, TData>>
    request?: SecondParameter<typeof customInstance>
  },
  queryClient?: QueryClient,
): UseQueryReturnType<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getGetUnreadCountQueryOptions(options)

  const query = useQuery(queryOptions, queryClient) as UseQueryReturnType<TData, TError> & {
    queryKey: DataTag<QueryKey, TData, TError>
  }

  query.queryKey = unref(queryOptions).queryKey as DataTag<QueryKey, TData, TError>

  return query
}

/**
 * Deletes a specific notification by its ID
 * @summary Delete notification
 */
export const deleteNotification = (
  id: MaybeRef<string>,
  options?: SecondParameter<typeof customInstance>,
) => {
  id = unref(id)

  return customInstance<void>(
    { url: `http://localhost:8080/api/notifications/${id}`, method: 'DELETE' },
    options,
  )
}

export const getDeleteNotificationMutationOptions = <
  TError = ErrorType<void>,
  TContext = unknown,
>(options?: {
  mutation?: UseMutationOptions<
    Awaited<ReturnType<typeof deleteNotification>>,
    TError,
    { id: string },
    TContext
  >
  request?: SecondParameter<typeof customInstance>
}): UseMutationOptions<
  Awaited<ReturnType<typeof deleteNotification>>,
  TError,
  { id: string },
  TContext
> => {
  const mutationKey = ['deleteNotification']
  const { mutation: mutationOptions, request: requestOptions } = options
    ? options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey
      ? options
      : { ...options, mutation: { ...options.mutation, mutationKey } }
    : { mutation: { mutationKey }, request: undefined }

  const mutationFn: MutationFunction<
    Awaited<ReturnType<typeof deleteNotification>>,
    { id: string }
  > = (props) => {
    const { id } = props ?? {}

    return deleteNotification(id, requestOptions)
  }

  return { mutationFn, ...mutationOptions }
}

export type DeleteNotificationMutationResult = NonNullable<
  Awaited<ReturnType<typeof deleteNotification>>
>

export type DeleteNotificationMutationError = ErrorType<void>

/**
 * @summary Delete notification
 */
export const useDeleteNotification = <TError = ErrorType<void>, TContext = unknown>(
  options?: {
    mutation?: UseMutationOptions<
      Awaited<ReturnType<typeof deleteNotification>>,
      TError,
      { id: string },
      TContext
    >
    request?: SecondParameter<typeof customInstance>
  },
  queryClient?: QueryClient,
): UseMutationReturnType<
  Awaited<ReturnType<typeof deleteNotification>>,
  TError,
  { id: string },
  TContext
> => {
  const mutationOptions = getDeleteNotificationMutationOptions(options)

  return useMutation(mutationOptions, queryClient)
}
